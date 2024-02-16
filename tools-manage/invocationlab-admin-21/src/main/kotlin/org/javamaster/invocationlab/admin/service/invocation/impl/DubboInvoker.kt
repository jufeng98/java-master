package org.javamaster.invocationlab.admin.service.invocation.impl

import org.javamaster.invocationlab.admin.consts.Constant
import org.javamaster.invocationlab.admin.consts.Constant.GROUP_DEFAULT
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.invocation.AbstractInvoker
import org.javamaster.invocationlab.admin.service.invocation.Invocation
import org.javamaster.invocationlab.admin.service.invocation.Invoker
import org.javamaster.invocationlab.admin.service.invocation.entity.DubboParamValue
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest
import com.alibaba.dubbo.config.ApplicationConfig
import com.alibaba.dubbo.config.ReferenceConfig
import com.alibaba.dubbo.config.RegistryConfig
import com.alibaba.dubbo.rpc.service.GenericService
import org.apache.commons.lang3.StringUtils
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.set

/**
 * @author yudong
 */
@Suppress("VulnerableCodeUsages")
@Primary
@Service
internal class DubboInvoker : AbstractInvoker(), Invoker<Any, PostmanDubboRequest> {
    private val application = ApplicationConfig(Constant.APP_NAME)
    private val cachedReference: MutableMap<String, ReferenceConfig<GenericService>> = WeakHashMap()

    override fun invoke(request: PostmanDubboRequest, invocation: Invocation): WebApiRspDto<Any> {
        val rpcParamValue = converter.convert(request, invocation)
        val service = getOrCreateService(request, rpcParamValue)
        val start = System.currentTimeMillis()
        val obj = service.`$invoke`(
            invocation.getJavaMethodName(), rpcParamValue.paramTypeNames
                .toArray { arrayOf<String>() }, rpcParamValue.paramValues.toTypedArray()
        )
        val end = System.currentTimeMillis()
        val elapse = end - start
        logger.info("请求dubbo耗时:$elapse")
        return WebApiRspDto.success(obj, elapse)
    }

    override fun invoke(pair: Pair<PostmanDubboRequest, Invocation>): WebApiRspDto<Any> {
        return invoke(pair.left, pair.right)
    }


    private fun getOrCreateService(request: PostmanDubboRequest, rpcParamValue: DubboParamValue): GenericService {
        val serviceName = request.serviceName
        val group = request.group
        val interfaceName = request.interfaceName
        val referenceKey = "$serviceName-$group-$interfaceName"

        val cacheKey = if (rpcParamValue.isUseDubbo) {
            rpcParamValue.dubboUrl + "-" + referenceKey
        } else {
            rpcParamValue.registry + "-" + referenceKey
        }
        var reference = cachedReference[cacheKey]
        if (reference == null) {
            synchronized(DubboInvoker::class.java) {
                reference = cachedReference[cacheKey]
                if (reference == null) {
                    var service: GenericService?
                    try {
                        reference = createReference(request, rpcParamValue)
                        service = reference!!.get()
                    } catch (e: Exception) {
                        //尝试不设置版本号
                        request.version = null
                        reference = createReference(request, rpcParamValue)
                        service = reference!!.get()
                    }
                    //如果创建成功了就添加，否则不添加
                    if (service != null) {
                        cachedReference[cacheKey] = reference!!
                    }
                }
            }
        }
        val service = reference!!.get()
        //如果创建失败了,比如provider重启了,需要重新创建
        if (service == null) {
            cachedReference.remove(cacheKey)
            throw Exception("ReferenceConfig创建GenericService失败,检查provider是否启动")
        }
        return service
    }

    fun createReference(request: PostmanDubboRequest, rpcParamValue: DubboParamValue): ReferenceConfig<GenericService> {
        val newReference = ReferenceConfig<GenericService>()
        newReference.timeout = 10000
        newReference.application = application
        newReference.setInterface(request.interfaceName)
        val group = request.group
        //default是我加的,dubbo默认是没有的
        if (StringUtils.isNotBlank(group) && GROUP_DEFAULT != group) {
            newReference.group = group
        }
        if (rpcParamValue.isUseDubbo) {
            //直连
            newReference.url = rpcParamValue.dubboUrl
            logger.info("直连dubbo地址:" + rpcParamValue.dubboUrl)
        } else {
            //通过zk访问
            newReference.setRegistry(RegistryConfig(rpcParamValue.registry))
        }
        newReference.version = request.version
        newReference.isGeneric = true
        //hard code
        newReference.retries = 1
        return newReference
    }
}
