package org.javamaster.invocationlab.admin.service.registry.impl

import org.javamaster.invocationlab.admin.service.registry.Register
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo
import org.javamaster.invocationlab.admin.util.BuildUtils.buildInterfaceKey
import org.javamaster.invocationlab.admin.util.ExecutorUtils.startAsyncTask
import org.javamaster.invocationlab.admin.util.SpringUtils
import com.alibaba.dubbo.common.Constants
import com.alibaba.dubbo.common.URL
import org.I0Itec.zkclient.IZkChildListener
import org.I0Itec.zkclient.ZkClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.CollectionUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * @author yudong
 */
@Suppress("VulnerableCodeUsages")
class ZkRegister(cluster: String) : Register {
    private val allProviders: MutableMap<String, MutableMap<String, InterfaceMetaInfo>> = ConcurrentHashMap()
    private val client = ZkClient(cluster, 5000)
    private val listeners: MutableMap<String, IZkChildListener> = HashMap()
    private val logger: Logger = LoggerFactory.getLogger(ZkRegister::class.java)

    init {
        this.pullData()
    }

    override fun tryPullDataAgain(interfaceNames: List<String>) {
        interfaceNames.forEach(Consumer { name: String ->
            try {
                val path = "$DUBBO_ROOT/$name/providers"
                val children = client.getChildren(path)
                processChildNodes(children)
            } catch (e: Exception) {
                logger.error("{}", name, e)
            }
        })
    }

    override fun pullData() {
        //第一次获取所有的子节点
        val dubboNodes = client.getChildren(DUBBO_ROOT)
        processDubboNodes(dubboNodes)
        startAsyncTask {
            //处理新增或者删除的节点
            client.subscribeChildChanges(
                DUBBO_ROOT
            ) { _: String?, currentChildes: List<String?> ->
                if (CollectionUtils.isEmpty(currentChildes)) {
//                            logger.info("dubbo no children");
                    return@subscribeChildChanges
                }
                //                        logger.info("dubbo目录下变更节点数量:" + currentChildes.size());
                processDubboNodes(currentChildes)
            }
        }
    }

    override fun getAllService(): Map<String, MutableMap<String, InterfaceMetaInfo>> {
        return allProviders
    }

    /**
     * @param dubboNodes 路径是:/dubbo节点下的所以子节点
     */
    private fun processDubboNodes(dubboNodes: List<String?>) {
        logger.info("provider的数量:" + dubboNodes.size)
        //避免重复订阅
        dubboNodes.parallelStream()
            .map { child: String? -> "$DUBBO_ROOT/$child/providers" }
            .forEach { childPath: String ->
                //添加变更监听
                listeners.putIfAbsent(
                    childPath,
                    IZkChildListener putIfAbsent@{ _: String?, currentChildes: List<String?> ->
                        if (CollectionUtils.isEmpty(currentChildes)) {
                            // logger.info("providers no children");
                            return@putIfAbsent
                        }
                        // logger.info("providers目录下变更节点数量:" + parentPath + " " + currentChildes.size());
                        processChildNodes(currentChildes)
                    })
                val children = client.getChildren(childPath)
                processChildNodes(children)
            }
        if (!SpringUtils.proEnv) {
            startAsyncTask {
                listeners.forEach { (path: String?, listener: IZkChildListener?) ->
                    client.subscribeChildChanges(
                        path,
                        listener
                    )
                }
            }
        }
    }


    private fun processChildNodes(children: List<String?>) {
        //serviceName,serviceKey,provider的其他属性信息
        val applicationNameMap: MutableMap<String, MutableMap<String, InterfaceMetaInfo>> = HashMap()
        children.forEach(Consumer<String?> forEach@{ child: String? ->
            var newChild = child
            try {
                newChild = URLDecoder.decode(newChild, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException(e)
            }
            val dubboUrl = URL.valueOf(newChild)
            val serviceName = dubboUrl.getParameter("application")
            val host = dubboUrl.host
            val port = dubboUrl.port
            val addr = "$host:$port"
            val version = dubboUrl.getParameter("version", "")
            val methods = dubboUrl.getParameter("methods")
            val group = dubboUrl.getParameter(Constants.GROUP_KEY, "default")
            val methodArray = methods.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val methodSets = mutableSetOf<String>()
            Collections.addAll(methodSets, *methodArray)
            val providerName = dubboUrl.getParameter("interface", "")
            if (providerName.isEmpty()) {
                logger.info("providerName empty")
                return@forEach
            }
            val interfaceKey = buildInterfaceKey(group, providerName, version)
            val metaItem = InterfaceMetaInfo()
            metaItem.interfaceName = providerName
            metaItem.group = group
            metaItem.applicationName = serviceName
            metaItem.methodNames = methodSets
            metaItem.version = version
            metaItem.serviceAddr = newChild
            metaItem.serverIps.plus(addr)

            //替换策略
            if (applicationNameMap.containsKey(serviceName)) {
                val oldMap = applicationNameMap[serviceName]!!
                //添加
                if (oldMap.containsKey(interfaceKey)) {
                    val providerItemOld = oldMap[interfaceKey]
                    providerItemOld!!.serverIps.plus(addr)
                } else {
                    oldMap[interfaceKey] = metaItem
                }
            } else {
                val oldMap: MutableMap<String, InterfaceMetaInfo> = HashMap()
                oldMap[interfaceKey] = metaItem
                applicationNameMap[serviceName] = oldMap
            }
        })

        applicationNameMap.keys
            .forEach(Consumer { serviceName: String ->
                if (allProviders.containsKey(serviceName)) {
                    val oldMap = allProviders[serviceName]!!
                    val newMap: Map<String, InterfaceMetaInfo> = applicationNameMap[serviceName]!!
                    //这里相当于替换和部分增加
                    oldMap.putAll(newMap)
                } else {
                    allProviders[serviceName] = applicationNameMap[serviceName]!!
                }
            })
    }

    companion object {
        const val DUBBO_ROOT: String = "/dubbo"
    }
}
