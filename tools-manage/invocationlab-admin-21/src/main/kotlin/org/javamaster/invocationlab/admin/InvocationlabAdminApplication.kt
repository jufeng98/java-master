package org.javamaster.invocationlab.admin

import org.javamaster.invocationlab.admin.util.SpringUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.web.socket.config.annotation.EnableWebSocket
import java.util.*

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@EnableAspectJAutoProxy
@EnableWebSocket
@EnableFeignClients(
    "org.javamaster.invocationlab.admin.feign"
)
@ServletComponentScan(
    "org.javamaster.invocationlab.admin.filter"
)
class InvocationlabAdminApplication

val log: Logger = LoggerFactory.getLogger(InvocationlabAdminApplication::class.java)

fun main(args: Array<String>) {
    log.info("开始启动 InvocationlabAdminApplication")
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"))
    val app = SpringApplication(InvocationlabAdminApplication::class.java)
    app.setBannerMode(Banner.Mode.CONSOLE)
    val env = System.getenv("ENV")
    if (StringUtils.equals(env, "PRO")) {
        app.setAdditionalProfiles(env.lowercase(Locale.getDefault()))
        SpringUtils.proEnv = true
    } else if (StringUtils.equals(env, "DEV")) {
        SpringUtils.devEnv = true
    }
    app.run(*args)
    log.info("InvocationlabAdminApplication 启动成功,当前环境:{}!", env)
}
