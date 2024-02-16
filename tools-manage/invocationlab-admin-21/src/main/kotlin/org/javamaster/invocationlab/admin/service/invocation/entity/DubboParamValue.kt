package org.javamaster.invocationlab.admin.service.invocation.entity

/**
 * @author yudong
 */

class DubboParamValue : RpcParamValue {
    var paramTypeNames: MutableList<String> = mutableListOf()
    var paramValues: MutableList<Any> = mutableListOf()
    var registry: String? = null
    var dubboUrl = "dubbo://ip"
    var isUseDubbo = false

    fun addParamTypeName(typeName: String) {
        paramTypeNames.add(typeName)
    }

    fun addParamValue(paramValue: Any) {
        paramValues.add(paramValue)
    }
}
