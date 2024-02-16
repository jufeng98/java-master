package org.javamaster.invocationlab.admin.service.invocation.impl

import org.javamaster.invocationlab.admin.consts.Constant.FEIGN_PARAM
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.context.InvokeContext.getFeignContext
import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam
import org.javamaster.invocationlab.admin.service.invocation.AbstractInvoker
import org.javamaster.invocationlab.admin.service.invocation.Invocation
import org.javamaster.invocationlab.admin.service.invocation.Invoker
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest
import org.javamaster.invocationlab.admin.util.BuildUtils.buildServiceKey
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils.remove
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils.set
import org.springframework.stereotype.Service

/**
 * @author yudong
 */
@Service
internal class FeignInvoker : AbstractInvoker(), Invoker<Any, PostmanDubboRequest> {

    override fun invoke(request: PostmanDubboRequest, invocation: Invocation): WebApiRspDto<Any> {
        val serviceKey = buildServiceKey(request.cluster!!, request.serviceName!!)
        val context = getFeignContext(serviceKey)

        val feignClz = context!!.classLoader!!.loadClass(request.interfaceName)
        val feignService = context.getBean(feignClz)

        val parameterTypes: Array<Class<*>> = invocation.getParams().stream()
            .map(RequestParam::targetParaType)
            .toArray { arrayOf<Class<*>>() }
        val method = feignClz.getDeclaredMethod(invocation.getJavaMethodName(), *parameterTypes)
        method.isAccessible = true

        val rpcParamValue = converter.convert(request, invocation)
        try {
            set(FEIGN_PARAM, Pair(request, rpcParamValue))
            val res = method.invoke(feignService, *rpcParamValue.paramValues.toTypedArray<Any>())
            return WebApiRspDto.success(res)
        } finally {
            remove(FEIGN_PARAM)
        }
    }

    override fun invoke(pair: Pair<PostmanDubboRequest, Invocation>): WebApiRspDto<Any> {
        return invoke(pair.left, pair.right)
    }
}
