package org.javamaster.invocationlab.admin.service.registry.impl;

import org.javamaster.invocationlab.admin.service.registry.Register;
import org.springframework.stereotype.Component;

/**
 * @author yudong
 */
@Component
public class DubboRegisterFactory extends AbstractRegisterFactory {

    @Override
    public Register create(String cluster) {
        return new ZkRegister(cluster);
    }

}
