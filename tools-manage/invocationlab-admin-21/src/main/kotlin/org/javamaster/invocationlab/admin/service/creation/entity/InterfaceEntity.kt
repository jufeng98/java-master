package org.javamaster.invocationlab.admin.service.creation.entity

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * @author yudong
 */

class InterfaceEntity {
    var key: String? = null

    var interfaceName: String? = null

    var serverIps: Set<String> = mutableSetOf()

    @JsonIgnore
    var interfaceClass: Class<*>? = null

    var methodNames: Set<String> = mutableSetOf()

    var methods: List<MethodEntity> = mutableListOf()

    var group: String? = null

    var version: String? = null

    var timeout: Long = 0

    var registryBeanName: String? = null

    var check: Boolean = false

    var retries: Int = 0
}
