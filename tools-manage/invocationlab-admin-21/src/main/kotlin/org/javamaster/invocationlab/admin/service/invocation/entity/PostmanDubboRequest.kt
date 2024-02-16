package org.javamaster.invocationlab.admin.service.invocation.entity

/**
 * @author yudong
 */
class PostmanDubboRequest : PostmanRequest {
    var dubboParam: String? = null
    var cluster: String? = null
    var serviceName: String? = null
    var group: String? = null
    var interfaceName: String? = null
    var version: String? = null
    var methodName: String? = null
    var dubboIp: String? = null
}
