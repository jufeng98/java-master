package org.javamaster.invocationlab.admin.service.registry.impl;

import org.javamaster.invocationlab.admin.service.registry.Register;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author yudong
 */
@Component
public class EurekaRegisterFactory extends AbstractRegisterFactory {
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Register create(String cluster) {
        return new EurekaRegister(cluster, restTemplate);
    }

}
