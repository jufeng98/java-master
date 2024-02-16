package org.javamaster.invocationlab.admin.service.registry.impl

import org.javamaster.invocationlab.admin.service.registry.Register
import org.springframework.stereotype.Component

/**
 * @author yudong
 */
@Component
class DubboRegisterFactory : AbstractRegisterFactory() {
    override fun create(cluster: String): Register {
        return ZkRegister(cluster)
    }

    override fun refreshService(interfaceNames: List<String>, cluster: String) {
        val register = get(cluster)
        register.tryPullDataAgain(interfaceNames)
    }
}
