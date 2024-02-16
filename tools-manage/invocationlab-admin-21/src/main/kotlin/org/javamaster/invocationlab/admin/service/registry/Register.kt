package org.javamaster.invocationlab.admin.service.registry

import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo

/**
 * @author yudong
 */
interface Register {
    fun tryPullDataAgain(interfaceNames: List<String>) {}

    fun pullData()

    fun getAllService(): Map<String, Map<String, InterfaceMetaInfo>>

    fun getServiceInstances(serviceName: String): List<String> {
        return emptyList()
    }
}
