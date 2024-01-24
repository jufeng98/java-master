package org.javamaster.invocationlab.admin.service.registry;

import java.util.List;
import java.util.Set;

/**
 * @author yudong
 */
public interface RegisterFactory {

    void addCluster(String cluster);

    Register remove(String cluster);

    Register get(String cluster);

    default void refreshService(List<String> interfaceNames, String cluster){}

    Set<String> getClusterSet();
}
