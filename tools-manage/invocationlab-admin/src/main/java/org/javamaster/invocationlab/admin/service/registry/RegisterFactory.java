package org.javamaster.invocationlab.admin.service.registry;

import java.util.Set;

/**
 * @author yudong
 */
public interface RegisterFactory {

    void addCluster(String cluster);

    Register remove(String cluster);

    Register get(String cluster);

    Set<String> getClusterSet();
}
