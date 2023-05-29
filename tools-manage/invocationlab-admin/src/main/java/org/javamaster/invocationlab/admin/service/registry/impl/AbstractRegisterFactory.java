package org.javamaster.invocationlab.admin.service.registry.impl;

import org.javamaster.invocationlab.admin.service.registry.Register;
import org.javamaster.invocationlab.admin.service.registry.RegisterFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author yudong
 */
public abstract class AbstractRegisterFactory implements RegisterFactory {
    protected Map<String, Register> allRegisters = new HashMap<>();
    protected Set<String> clusterSet = new HashSet<>();

    @Override
    public void addCluster(String cluster) {
        clusterSet.add(cluster);
    }

    @Override
    public Register get(String cluster) {
        if (allRegisters.containsKey(cluster)) {
            return allRegisters.get(cluster);
        }
        Register register = create(cluster);
        allRegisters.put(cluster, register);
        return register;
    }

    @Override
    public Register remove(String cluster) {
        return allRegisters.remove(cluster);
    }

    public abstract Register create(String cluster);

    @Override
    public Set<String> getClusterSet() {
        return clusterSet;
    }
}
