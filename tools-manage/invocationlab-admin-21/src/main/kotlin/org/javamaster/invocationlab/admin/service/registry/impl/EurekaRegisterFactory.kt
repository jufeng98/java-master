package org.javamaster.invocationlab.admin.service.registry.impl

import org.javamaster.invocationlab.admin.service.registry.Register
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 * @author yudong
 */
@Component
class EurekaRegisterFactory : AbstractRegisterFactory() {
    @Autowired
    private lateinit var restTemplate: RestTemplate

    override fun create(cluster: String): Register {
        return EurekaRegister(cluster, restTemplate)
    }
}
