package org.javamaster.invocationlab.admin.service.registry.impl;

import org.javamaster.invocationlab.admin.service.registry.Register;
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.ExecutorUtils;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import lombok.SneakyThrows;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yudong
 */
public class ZkRegister implements Register {
    final static String DUBBO_ROOT = "/dubbo";
    private final Map<String, Map<String, InterfaceMetaInfo>> allProviders = new ConcurrentHashMap<>();
    private final ZkClient client;
    private final Map<String, IZkChildListener> listeners = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ZkRegister.class);

    public ZkRegister(String cluster) {
        client = new ZkClient(cluster, 5000);
        this.pullData();
    }

    @Override
    public void pullData() {
        //第一次获取所有的子节点
        List<String> dubboNodes = client.getChildren(DUBBO_ROOT);
        processDubboNodes(dubboNodes);
        ExecutorUtils.startAsyncTask(() -> {
            //处理新增或者删除的节点
            client.subscribeChildChanges(DUBBO_ROOT,
                    (parentPath, currentChildes) -> {
                        if (CollectionUtils.isEmpty(currentChildes)) {
                            return;
                        }
                        logger.debug("dubbo目录下变更节点数量:" + currentChildes.size());
                        processDubboNodes(currentChildes);
                    });
        });
    }

    @Override
    public Map<String, Map<String, InterfaceMetaInfo>> getAllService() {
        return allProviders;
    }

    /**
     * @param dubboNodes 路径是:/dubbo节点下的所以子节点
     */
    private void processDubboNodes(List<String> dubboNodes) {
        logger.info("provider的数量:" + dubboNodes.size());
        //避免重复订阅
        dubboNodes.parallelStream()
                .map(child -> DUBBO_ROOT + "/" + child + "/providers")
                .forEach(childPath -> {
                    if (!listeners.containsKey(childPath)) {
                        ExecutorUtils.startAsyncTask(() -> {
                            //添加变更监听
                            listeners.put(childPath,
                                    (parentPath, currentChildes) -> {
                                        if (CollectionUtils.isEmpty(currentChildes)) {
                                            return;
                                        }
                                        logger.debug("providers目录下变更节点数量:" + currentChildes.size());
                                        processChildNodes(currentChildes);
                                    });
                        });
                    }
                    List<String> children1 = client.getChildren(childPath);
                    processChildNodes(children1);
                });

        ExecutorUtils.startAsyncTask(() -> listeners.forEach(client::subscribeChildChanges));
    }

    @SneakyThrows
    private void processChildNodes(List<String> children1) {
        //serviceName,serviceKey,provider的其他属性信息
        Map<String, Map<String, InterfaceMetaInfo>> applicationNameMap = new HashMap<>();

        children1.forEach(child1 -> {
            try {
                child1 = URLDecoder.decode(child1, "utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            URL dubboUrl = URL.valueOf(child1);
            String serviceName = dubboUrl.getParameter("application");
            String host = dubboUrl.getHost();
            int port = dubboUrl.getPort();
            String addr = host + ":" + port;
            String version = dubboUrl.getParameter("version", "");
            String methods = dubboUrl.getParameter("methods");
            String group = dubboUrl.getParameter(Constants.GROUP_KEY, "default");
            String[] methodArray = methods.split(",");
            Set<String> methodSets = new HashSet<>();
            Collections.addAll(methodSets, methodArray);
            String providerName = dubboUrl.getParameter("interface", "");
            if (providerName.isEmpty()) {
                return;
            }
            String interfaceKey = BuildUtils.buildInterfaceKey(group, providerName, version);
            InterfaceMetaInfo metaItem = new InterfaceMetaInfo();
            metaItem.setInterfaceName(providerName);
            metaItem.setGroup(group);
            metaItem.setApplicationName(serviceName);
            metaItem.setMethodNames(methodSets);
            metaItem.setVersion(version);
            metaItem.setServiceAddr(child1);
            metaItem.getServerIps().add(addr);

            //替换策略
            if (applicationNameMap.containsKey(serviceName)) {
                Map<String, InterfaceMetaInfo> oldMap = applicationNameMap.get(serviceName);
                //添加
                if (oldMap.containsKey(interfaceKey)) {
                    InterfaceMetaInfo providerItemOld = oldMap.get(interfaceKey);
                    providerItemOld.getServerIps().add(addr);
                } else {
                    oldMap.put(interfaceKey, metaItem);
                }
            } else {
                Map<String, InterfaceMetaInfo> oldMap = new HashMap<>();
                oldMap.put(interfaceKey, metaItem);
                applicationNameMap.put(serviceName, oldMap);
            }
        });

        applicationNameMap.keySet()
                .forEach(serviceName -> {
                    if (allProviders.containsKey(serviceName)) {
                        Map<String, InterfaceMetaInfo> oldMap = allProviders.get(serviceName);
                        Map<String, InterfaceMetaInfo> newMap = applicationNameMap.get(serviceName);
                        //这里相当于替换和部分增加
                        oldMap.putAll(newMap);
                    } else {
                        allProviders.put(serviceName, applicationNameMap.get(serviceName));
                    }
                });
    }
}
