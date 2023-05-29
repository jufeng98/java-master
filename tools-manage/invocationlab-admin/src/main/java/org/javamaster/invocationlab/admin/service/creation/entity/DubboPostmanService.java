package org.javamaster.invocationlab.admin.service.creation.entity;

import org.javamaster.invocationlab.admin.service.GAV;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 */
@Data
public class DubboPostmanService implements PostmanService {

    String cluster;

    String serviceName;

    GAV gav = new GAV();

    long generateTime;

    /**
     * 标识是否加载到classLoader
     * 这个值不能持久化
     */
    @JsonIgnore
    Boolean loadedToClassLoader = false;

    /**
     * 一个dubbo应用包含多个接口定义
     */
    List<InterfaceEntity> interfaceModels = new ArrayList<>();

    @Override
    public String getCluster() {
        return cluster;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public GAV getGav() {
        return gav;
    }

    @Override
    public List<InterfaceEntity> getInterfaceModels() {
        return interfaceModels;
    }

    @Override
    public boolean getLoadedToClassLoader() {
        return loadedToClassLoader;
    }

    @Override
    public void setLoadedToClassLoader(boolean load) {

        this.loadedToClassLoader = load;
    }
}
