package org.javamaster.invocationlab.admin.service.invocation.entity

import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam
import org.javamaster.invocationlab.admin.service.invocation.Invocation

/**
 * @author yudong
 */
class DubboInvocation : Invocation {
    private var javaMethodName: String? = null
    private var requestParams: List<RequestParam>? = null

    override fun getJavaMethodName(): String {
        return javaMethodName!!
    }

    override fun setJavaMethodName(javaMethodName: String) {
        this.javaMethodName = javaMethodName
    }

    override fun getParams(): List<RequestParam> {
        return requestParams!!
    }

    override fun setRequestParams(requestParams: List<RequestParam>) {
        this.requestParams = requestParams
    }
}
