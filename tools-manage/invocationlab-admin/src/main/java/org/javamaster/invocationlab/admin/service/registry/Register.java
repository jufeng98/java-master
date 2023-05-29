package org.javamaster.invocationlab.admin.service.registry;

import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author yudong
 */
public interface Register {

    void pullData();

    Map<String, Map<String, InterfaceMetaInfo>> getAllService();

    default List<String> getServiceInstances(String serviceName) {
        return Collections.emptyList();
    }
}
