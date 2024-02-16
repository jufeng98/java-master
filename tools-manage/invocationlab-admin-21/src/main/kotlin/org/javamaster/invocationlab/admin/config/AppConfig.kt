package org.javamaster.invocationlab.admin.config

import org.javamaster.invocationlab.admin.consts.Constant
import org.javamaster.invocationlab.admin.service.maven.Maven
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.task.BackupProjectTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 启动的时候systemInit()加载需要的zk地址和已经创建的服务
 *
 * @author yudong
 */
@Configuration
class AppConfig {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${dubbo.api.jar.dir}")
    private lateinit var apiJarPath: String

    @Value("\${nexus.url}")
    private lateinit var nexusPath: String

    @Value("\${nexus.url.releases}")
    private lateinit var nexusPathReleases: String

    @Autowired
    private lateinit var redisRepository: RedisRepository

    @Bean
    fun mavenProcessor(): Maven {
        return Maven(nexusPath, nexusPathReleases, apiJarPath)
    }

    @Bean
    fun initializer(): Initializer {
        val initializer = Initializer()

        //统一设置路径入口,其他地方通过System.getProperty获取
        System.setProperty(Constant.USER_HOME, apiJarPath)

        initializer.copySettingXml(apiJarPath)

        try {
            initializer.loadZkAddress(redisRepository)
        } catch (e: Exception) {
            log.error("zookeeper down", e)
        }

        try {
            initializer.loadCreatedService(redisRepository, RedisKeys.RPC_MODEL_KEY)
        } catch (e: Exception) {
            log.error("redis down", e)
        }

        initializer.initGlobalJdkClassLoader()

        BackupProjectTask.startTask()

        return initializer
    }
}
