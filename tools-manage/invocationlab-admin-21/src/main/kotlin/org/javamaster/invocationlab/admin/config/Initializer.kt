package org.javamaster.invocationlab.admin.config

import org.javamaster.invocationlab.admin.service.AppFactory
import org.javamaster.invocationlab.admin.service.context.InvokeContext
import org.javamaster.invocationlab.admin.service.creation.PostmanService
import org.javamaster.invocationlab.admin.service.creation.entity.DubboPostmanService
import org.javamaster.invocationlab.admin.service.creation.impl.JdkCreator
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.util.FileUtils
import org.javamaster.invocationlab.admin.util.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.util.function.Consumer
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * 执行系统启动的时候需要加载的配置
 *
 * @author yudong
 */
class Initializer {
    private val logger: Logger = LoggerFactory.getLogger(Initializer::class.java)

    fun loadCreatedService(
        redisRepository: RedisRepository,
        dubboModelRedisKey: String
    ) {
        val serviceKeys = redisRepository.mapGetKeys<Any>(dubboModelRedisKey)
        logger.info("已经创建的服务数量:" + serviceKeys.size)
        serviceKeys.parallelStream()
            .forEach { serviceKey: Any? ->
                val dubboModelString = redisRepository.mapGet<String>(dubboModelRedisKey, serviceKey!!)
                val postmanService: PostmanService =
                    JsonUtils.parseObject(dubboModelString, DubboPostmanService::class.java)
                InvokeContext.putService(serviceKey as String, postmanService)
            }
        logger.info("完成初始化的服务数量:" + serviceKeys.size)
    }

    fun copySettingXml(userHomePath: String) {
        val file = File("$userHomePath/.m2/settings.xml")
        if (file.exists()) {
            file.delete()
        }
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        //复制文件内容
        changLocalRepository("$userHomePath/.m2")
    }

    /**
     * 把setting.xml文件里面的localRepository改成服务器上的绝对目录
     */
    private fun changLocalRepository(newPath: String) {
        var path = newPath
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()

        val url = this.javaClass.classLoader.getResource("config/setting.xml")
        val content = FileUtils.readStringFromUrl(url!!)
        val bytes = content.toByteArray()

        ByteArrayInputStream(bytes).use {
            val doc = dBuilder.parse(it)
            doc.documentElement.normalize()
            logger.info("Root element :" + doc.documentElement.nodeName)

            val nList = doc.getElementsByTagName("localRepository")
            val oldText = nList.item(0).textContent
            logger.info("setting.xml的localRepository旧值:$oldText")
            val newContent = "$path/repository"
            nList.item(0).textContent = newContent
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            val source = DOMSource(doc)
            path = "$path/settings.xml"
            logger.info("setting.xml路径:$path")

            val result = StreamResult(File(path))
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.transform(source, result)
            logger.info("setting.xml 更新成功")
        }
    }

    fun loadZkAddress(redisRepository: RedisRepository) {
        val zkList = redisRepository.members<Any>(RedisKeys.CLUSTER_REDIS_KEY)
        if (zkList.isNullOrEmpty()) {
            //系统第一次使用
            logger.warn("没有配置任何集群地址,请通过web页面添加集群地址")
            return
        }

        logger.info("系统当前已经添加的集群地址:$zkList")
        zkList.forEach(Consumer { cluster: Any? ->
            val type = redisRepository.mapGet<Int>(RedisKeys.CLUSTER_REDIS_KEY_TYPE, cluster!!) ?: return@Consumer
            val registerFactory = AppFactory.getRegisterFactory(type)
            registerFactory.addCluster(cluster as String)
            registerFactory.get(cluster)
        })
        logger.info("完成初始化集群地址:$zkList")
    }

    fun initGlobalJdkClassLoader() {
        JdkCreator.initGlobalJdkClassLoader()
    }
}
