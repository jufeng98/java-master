package org.javamaster.invocationlab.admin.service.registry.impl;

import org.javamaster.invocationlab.admin.service.registry.Register;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yudong
 */
@Component
public class DubboRegisterFactory extends AbstractRegisterFactory {

    @Override
    public Register create(String cluster) {
        return new ZkRegister(cluster);
    }

    @Override
    public void refreshService(List<String> interfaceNames, String cluster) {
        Register register = get(cluster);
        register.tryPullDataAgain(interfaceNames);
    }
}
