package org.javamaster.invocationlab.admin.service.invocation.impl

import org.javamaster.invocationlab.admin.service.invocation.Converter
import org.javamaster.invocationlab.admin.service.invocation.Invocation
import org.javamaster.invocationlab.admin.service.invocation.entity.DubboParamValue
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest
import org.javamaster.invocationlab.admin.service.invocation.exception.ParamException
import org.javamaster.invocationlab.admin.util.BuildUtils.buildZkUrl
import org.javamaster.invocationlab.admin.util.JsonUtils
import org.javamaster.invocationlab.admin.util.JsonUtils.parseObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author yudong
 */
@Component
internal class DubboConverter : Converter<PostmanDubboRequest, DubboParamValue> {

    override fun convert(request: PostmanDubboRequest, invocation: Invocation): DubboParamValue {
        val bodyMap = parseObject(request.dubboParam, MutableMap::class.java)
        val rpcParamValue = DubboParamValue()
        //遍历模板的参数名称
        for (param in invocation.getParams()) {
            val paramName = param.paraName
            val targetType = param.targetParaType
            var nullParam = false
            var paramValue: Any? = null
            val value = bodyMap[paramName]
            if (value == null) {
                //传入null的参数
                nullParam = true
            } else {
                val old = Thread.currentThread().contextClassLoader
                Thread.currentThread().contextClassLoader = targetType!!.classLoader
                try {
                    paramValue = JsonUtils.mapper.convertValue(value, targetType)
                } catch (exp: Exception) {
                    logger.error("参数反序列化失败:$value,$targetType", exp)
                } finally {
                    Thread.currentThread().contextClassLoader = old
                }
            }
            if (!nullParam && paramValue == null) {
                throw ParamException("参数匹配错误,参数名称:" + paramName + ",请检查类型,参数类型:" + targetType!!.name)
            }
            rpcParamValue.addParamTypeName(targetType!!.name)
            rpcParamValue.addParamValue(paramValue!!)
        }
        parseExternalParams(request, rpcParamValue)
        return rpcParamValue
    }

    private fun parseExternalParams(request: PostmanDubboRequest, rpcParamValue: DubboParamValue) {
        if (request.dubboIp != null && request.dubboIp!!.isNotEmpty()) {
            val dubboIp = request.dubboIp
            rpcParamValue.dubboUrl = rpcParamValue.dubboUrl.replace("ip", dubboIp!!)
            rpcParamValue.isUseDubbo = true
        }
        if (request.cluster != null) {
            val zk = request.cluster
            val accessZk = buildZkUrl(zk!!)
            rpcParamValue.registry = accessZk
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DubboConverter::class.java)
    }
}
