package org.javamaster.invocationlab.admin.service.registry.entity

/**
 * @author yudong
 * 接口及接口包含的方法及参数等元数据,从zk获取
 * 如果从其他地方拉取这个数据类型应该是兼容的
 */

class InterfaceMetaInfo {
    var applicationName: String? = null
    var group: String? = null
    var version: String? = null
    var interfaceName: String? = null

    /**
     * 这个是zk拼接的完整地址:dubbo://192.....
     */
    var serviceAddr: String? = null
    var methodNames: Set<String> = mutableSetOf()
    var serverIps: Set<String> = mutableSetOf()
}
