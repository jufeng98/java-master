package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.service.AppFactory
import org.javamaster.invocationlab.admin.service.context.InvokeContext.buildInvocation
import org.javamaster.invocationlab.admin.service.context.InvokeContext.getService
import org.javamaster.invocationlab.admin.service.context.InvokeContext.tryLoadRuntimeInfo
import org.javamaster.invocationlab.admin.service.impl.RedisServiceImpl.Companion.tokenVo
import org.javamaster.invocationlab.admin.util.BuildUtils.buildServiceKey
import org.javamaster.invocationlab.admin.util.JsonUtils.objectToString
import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

/**
 * 访问dubbo的对外接口
 *
 * @author yudong
 */

@Controller
class RpcPostmanProxyController {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var appFactory: AppFactory

    @RequestMapping(value = ["/dubbo"], method = [RequestMethod.POST])
    @ResponseBody
    fun dubbo(@RequestBody jsonNode: JsonNode): Any {
        val tokenVo = tokenVo
        log.info(
            "{}-{} request dubbo or eureka:{}",
            tokenVo.userId,
            tokenVo.username,
            jsonNode
        )
        val cluster = jsonNode["cluster"].asText()
        val serviceName = jsonNode["serviceName"].asText()
        val interfaceKey = jsonNode["interfaceKey"].asText()
        val methodName = jsonNode["methodName"].asText()
        val dubboParam = jsonNode["dubboParam"].asText()
        val dubboIp = jsonNode["dubboIp"].asText()

        val type = appFactory.getType(cluster)
        val serviceKey = buildServiceKey(cluster, serviceName)
        val postmanService = getService(serviceKey)
        tryLoadRuntimeInfo(postmanService, type!!, serviceKey)

        val pair = buildInvocation(
            cluster, serviceName, interfaceKey,
            methodName, dubboParam, dubboIp
        )
        val request = pair.left
        val invocation = pair.right
        if (logger.isDebugEnabled) {
            logger.debug("接收RPC-POSTMAN请求:" + objectToString(request))
        }
        val invoker = appFactory.getInvoker(cluster)
        return invoker.invoke(request, invocation).data!!
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RpcPostmanProxyController::class.java)
    }
}
