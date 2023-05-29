package org.javamaster.invocationlab.admin.service.creation.entity;

import org.javamaster.invocationlab.admin.service.GAV;

import java.util.List;

/**
 * @author yudong
 */
public interface PostmanService {

    String getCluster();

    String getServiceName();

    GAV getGav();

    List<InterfaceEntity> getInterfaceModels();

    boolean getLoadedToClassLoader();

    void setLoadedToClassLoader(boolean load);
}
