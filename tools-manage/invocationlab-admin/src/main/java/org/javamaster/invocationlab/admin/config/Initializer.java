package org.javamaster.invocationlab.admin.config;

import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.creation.entity.DubboPostmanService;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.creation.impl.JdkCreator;
import org.javamaster.invocationlab.admin.service.registry.RegisterFactory;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.FileUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import lombok.Cleanup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

import static org.javamaster.invocationlab.admin.service.AppFactory.getRegisterFactory;

/**
 * 执行系统启动的时候需要加载的配置
 *
 * @author yudong
 */
public class Initializer {
    private final Logger logger = LoggerFactory.getLogger(Initializer.class);

    public void loadCreatedService(RedisRepository redisRepository,
                                   String dubboModelRedisKey) {
        Set<Object> serviceKeys = redisRepository.mapGetKeys(dubboModelRedisKey);
        logger.info("已经创建的服务数量:" + serviceKeys.size());
        serviceKeys.parallelStream()
                .forEach(serviceKey -> {
                    String dubboModelString = redisRepository.mapGet(dubboModelRedisKey, serviceKey);
                    PostmanService postmanService = JsonUtils.parseObject(dubboModelString, DubboPostmanService.class);
                    InvokeContext.putService((String) serviceKey, postmanService);
                });
        logger.info("完成初始化的服务数量:" + serviceKeys.size());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void copySettingXml(String userHomePath) throws Exception {
        File file = new File(userHomePath + "/.m2/settings.xml");
        if (file.exists()) {
            file.delete();
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        //复制文件内容
        changLocalRepository(userHomePath + "/" + ".m2");
    }

    /**
     * 把setting.xml文件里面的localRepository改成服务器上的绝对目录
     */
    void changLocalRepository(String newPath) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        URL url = this.getClass().getClassLoader().getResource("config/setting.xml");
        String content = FileUtils.readStringFromUrl(url);
        byte[] bytes = Objects.requireNonNull(content).getBytes();

        @Cleanup
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        Document doc = dBuilder.parse(bi);
        doc.getDocumentElement().normalize();
        logger.info("Root element :" + doc.getDocumentElement().getNodeName());

        NodeList nList = doc.getElementsByTagName("localRepository");
        String oldText = nList.item(0).getTextContent();
        logger.info("setting.xml的localRepository旧值:" + oldText);
        String newContent = newPath + "/repository";
        nList.item(0).setTextContent(newContent);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        newPath = newPath + "/settings.xml";
        logger.info("setting.xml路径:" + newPath);

        StreamResult result = new StreamResult(new File(newPath));
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
        logger.info("setting.xml 更新成功");
    }

    void loadZkAddress(RedisRepository redisRepository) {
        Set<Object> zkList = redisRepository.members(RedisKeys.CLUSTER_REDIS_KEY);
        if (zkList == null || zkList.isEmpty()) {
            //系统第一次使用
            logger.warn("没有配置任何集群地址,请通过web页面添加集群地址");
            return;
        }

        logger.info("系统当前已经添加的集群地址:" + zkList);
        zkList.forEach(cluster -> {
            Integer type = redisRepository.mapGet(RedisKeys.CLUSTER_REDIS_KEY_TYPE, cluster);
            RegisterFactory registerFactory = getRegisterFactory(type);
            registerFactory.addCluster((String) cluster);
            registerFactory.get((String) cluster);
        });
        logger.info("完成初始化集群地址:" + zkList);
    }

    void initGlobalJdkClassLoader() {
        JdkCreator.initGlobalJdkClassLoader();
    }
}
