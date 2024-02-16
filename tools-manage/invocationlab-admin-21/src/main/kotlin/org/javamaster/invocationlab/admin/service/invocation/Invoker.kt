package org.javamaster.invocationlab.admin.service.invocation

import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanRequest

/**
 * @author yudong
 */
interface Invoker<T, R : PostmanRequest?> {
    fun invoke(request: R, invocation: Invocation): WebApiRspDto<T>

    /**
     * 在js文件里面需要调用这个方法,方便进行场景测试
     */
    fun invoke(pair: Pair<R, Invocation>): WebApiRspDto<T>
}
