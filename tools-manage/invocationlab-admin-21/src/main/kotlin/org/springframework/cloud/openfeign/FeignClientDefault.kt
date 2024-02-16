package org.springframework.cloud.openfeign

import org.javamaster.invocationlab.admin.consts.Constant
import org.javamaster.invocationlab.admin.service.AppFactory
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.invocation.entity.DubboParamValue
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest
import org.javamaster.invocationlab.admin.util.SpringUtils
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils
import feign.Client
import feign.Request
import feign.Response
import org.apache.commons.lang3.RandomUtils
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

/**
 * @author yudong
 * @date 2022/11/9
 */
class FeignClientDefault @JvmOverloads constructor(
    sslContextFactory: SSLSocketFactory? = null,
    hostnameVerifier: HostnameVerifier? = null
) : Client.Default(sslContextFactory, hostnameVerifier) {

    override fun execute(request: Request, options: Request.Options): Response {
        var newRequest = request
        val pair = ThreadLocalUtils.get<Pair<PostmanDubboRequest, DubboParamValue>>(Constant.FEIGN_PARAM)
        val serviceName = pair!!.left.serviceName!!
        val appFactory = SpringUtils.context.getBean(AppFactory::class.java)
        val register = appFactory.getRegisterFactory(pair.left.cluster!!).get(pair.left.cluster!!)
        val host: String
        if (pair.right.isUseDubbo) {
            host = pair.right.dubboUrl.replace("dubbo://", "")
        } else {
            val instances = register.getServiceInstances(serviceName)
            val i = RandomUtils.nextInt(0, instances.size)
            host = instances[i]
        }
        val url = newRequest.url().replace(serviceName.lowercase(Locale.getDefault()), host)
        newRequest = Request.create(
            newRequest.httpMethod(),
            url,
            newRequest.headers(),
            newRequest.body(),
            newRequest.charset()
        )
        return super.execute(newRequest, options)
    }
}
