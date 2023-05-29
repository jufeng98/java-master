package org.javamaster.invocationlab.admin.service.invocation;

import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam;

import java.util.List;

/**
 * @author yudong
 */
public interface Invocation {

    /**
     * 获取原始的方法名称,方法名后面是不带括号和参数类型名的
     */
    String getJavaMethodName();

    void setJavaMethodName(String javaMethodName);

    List<RequestParam> getParams();

    void setRequestParams(List<RequestParam> requestParams);
}
