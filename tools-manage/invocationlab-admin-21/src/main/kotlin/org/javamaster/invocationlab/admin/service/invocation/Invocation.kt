package org.javamaster.invocationlab.admin.service.invocation

import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam

/**
 * @author yudong
 */
interface Invocation {
    /**
     * 获取原始的方法名称,方法名后面是不带括号和参数类型名的
     */
    fun getJavaMethodName(): String

    fun setJavaMethodName(javaMethodName: String)

    fun getParams(): List<RequestParam>

    fun setRequestParams(requestParams: List<RequestParam>)
}
