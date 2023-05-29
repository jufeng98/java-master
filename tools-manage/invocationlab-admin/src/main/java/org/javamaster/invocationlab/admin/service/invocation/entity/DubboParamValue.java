package org.javamaster.invocationlab.admin.service.invocation.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 */
public class DubboParamValue implements RpcParamValue {
    private final List<String> paramTypeNames = new ArrayList<>();
    private final List<Object> paramValues = new ArrayList<>();
    private String registry;
    private String dubboUrl = "dubbo://ip";
    private boolean useDubbo = false;

    public void addParamTypeName(String typeName) {
        paramTypeNames.add(typeName);
    }

    public List<String> getParamTypeNames() {
        return paramTypeNames;
    }

    public void addParamValue(Object paramValue) {
        paramValues.add(paramValue);
    }

    public List<Object> getParamValues() {
        return paramValues;
    }

    public String getDubboUrl() {
        return dubboUrl;
    }

    public void setDubboUrl(String dubboUrl) {
        this.dubboUrl = dubboUrl;
    }

    public boolean isUseDubbo() {
        return useDubbo;
    }

    public void setUseDubbo(boolean useDubbo) {
        this.useDubbo = useDubbo;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }
}
