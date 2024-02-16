package org.javamaster.invocationlab.admin.service.context

import org.javamaster.invocationlab.admin.enums.RegisterCenterType
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.creation.PostmanService
import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam
import org.javamaster.invocationlab.admin.service.creation.impl.EurekaCreator
import org.javamaster.invocationlab.admin.service.invocation.Invocation
import org.javamaster.invocationlab.admin.service.invocation.entity.DubboInvocation
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader
import org.javamaster.invocationlab.admin.util.BuildUtils.getGroupByInterfaceKey
import org.javamaster.invocationlab.admin.util.BuildUtils.getInterfaceNameByInterfaceKey
import org.javamaster.invocationlab.admin.util.BuildUtils.getJavaMethodName
import org.javamaster.invocationlab.admin.util.BuildUtils.getMethodNameKey
import org.javamaster.invocationlab.admin.util.BuildUtils.getVersionByInterfaceKey
import org.javamaster.invocationlab.admin.util.SpringUtils
import org.springframework.context.support.GenericApplicationContext
import java.util.concurrent.ConcurrentHashMap

/**
 * @author yudong
 */
object InvokeContext {
    private val CONTEXT_MAP: MutableMap<String, GenericApplicationContext> = ConcurrentHashMap()

    private val POSTMAN_SERVICE_MAP: MutableMap<String, PostmanService?> = ConcurrentHashMap()

    private val REQUESTPARAM_MAP: MutableMap<String, List<RequestParam>?> = ConcurrentHashMap()

    @JvmStatic
    fun getService(serviceKey: String): PostmanService? {
        return POSTMAN_SERVICE_MAP.getOrDefault(serviceKey, null)
    }

    private fun getRequestParam(methodNameKey: String): List<RequestParam> {
        return REQUESTPARAM_MAP.getOrDefault(methodNameKey, null)!!
    }

    @JvmStatic
    fun putService(serviceKey: String, service: PostmanService?) {
        POSTMAN_SERVICE_MAP[serviceKey] = service
    }

    @JvmStatic
    fun removeService(serviceKey: String) {
        POSTMAN_SERVICE_MAP.remove(serviceKey)
    }

    @JvmStatic
    fun putFeignContext(serviceKey: String, context: GenericApplicationContext) {
        CONTEXT_MAP[serviceKey] = context
    }

    @JvmStatic
    fun getFeignContext(serviceKey: String): GenericApplicationContext? {
        return CONTEXT_MAP[serviceKey]
    }

    fun putMethod(methodKey: String, requestParamList: List<RequestParam>?) {
        REQUESTPARAM_MAP[methodKey] = requestParamList
    }

    @JvmStatic
    fun tryLoadRuntimeInfo(postmanService: PostmanService?, type: RegisterCenterType, serviceKey: String) {
        if (postmanService == null) {
            return
        }

        if (postmanService.getLoadedToClassLoader()) {
            return
        }

        //服务重启的时候需要重新构建运行时信息
        JarLocalFileLoader.loadInterfaceInfoAndInitLoader(postmanService)

        if (type == RegisterCenterType.EUREKA) {
            val creator: EurekaCreator = SpringUtils.context.getBean(EurekaCreator::class.java)
            creator.initFeignInfoAndPutContext(postmanService, serviceKey)
        }
    }

    @JvmStatic
    fun buildInvocation(
        cluster: String?,
        serviceName: String?,
        interfaceKey: String?,
        methodName: String?,
        dubboParam: String?,
        dubboIp: String?
    ): Pair<PostmanDubboRequest, Invocation> {
        val request = PostmanDubboRequest()
        request.cluster = cluster
        request.serviceName = serviceName
        val group = getGroupByInterfaceKey(interfaceKey!!)
        request.group = group
        val interfaceName = getInterfaceNameByInterfaceKey(interfaceKey)
        request.interfaceName = interfaceName
        val version = getVersionByInterfaceKey(interfaceKey)
        request.version = version
        request.methodName = methodName
        request.dubboParam = dubboParam
        request.dubboIp = dubboIp

        val invocation: Invocation = DubboInvocation()
        val javaMethodName = getJavaMethodName(methodName!!)
        invocation.setJavaMethodName(javaMethodName)
        val methodNameKey = getMethodNameKey(cluster!!, serviceName!!, interfaceKey, methodName)
        val requestParamList = getRequestParam(methodNameKey)
        invocation.setRequestParams(requestParamList)

        return Pair(request, invocation)
    }
}
