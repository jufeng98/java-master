package org.javamaster.invocationlab.admin.service.invocation.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 */
@Getter
public class DubboParamValue implements RpcParamValue {
    private final List<String> paramTypeNames = new ArrayList<>();
    private final List<Object> paramValues = new ArrayList<>();
    @Setter
    private String registry;
    @Setter
    private String dubboUrl = "dubbo://ip";
    @Setter
    private boolean useDubbo = false;

    public void addParamTypeName(String typeName) {
        paramTypeNames.add(typeName);
    }

    public void addParamValue(Object paramValue) {
        paramValues.add(paramValue);
    }

}
