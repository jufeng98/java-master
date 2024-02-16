package org.javamaster.invocationlab.admin.service.invocation

import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanRequest
import org.javamaster.invocationlab.admin.service.invocation.entity.RpcParamValue

/**
 * @author yudong
 */
interface Converter<R : PostmanRequest, T : RpcParamValue> {
    fun convert(request: R, invocation: Invocation): T
}
