package org.javamaster.invocationlab.admin.service.invocation.entity;

import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam;
import org.javamaster.invocationlab.admin.service.invocation.Invocation;

import java.util.List;

/**
 * @author yudong
 */
public class DubboInvocation implements Invocation {
    String javaMethodName;
    List<RequestParam> requestParams;

    @Override
    public String getJavaMethodName() {
        return this.javaMethodName;
    }

    public void setJavaMethodName(String javaMethodName) {
        this.javaMethodName = javaMethodName;
    }

    @Override
    public List<RequestParam> getParams() {
        return this.requestParams;
    }

    public void setRequestParams(List<RequestParam> requestParams) {
        this.requestParams = requestParams;
    }
}
