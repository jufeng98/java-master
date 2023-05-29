package org.javamaster.invocationlab.admin.service.invocation;

import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanRequest;
import org.javamaster.invocationlab.admin.service.invocation.entity.RpcParamValue;
import org.javamaster.invocationlab.admin.service.invocation.exception.ParamException;

/**
 * @author yudong
 */
public interface Converter<R extends PostmanRequest, T extends RpcParamValue> {

    T convert(R request, Invocation invocation) throws ParamException;
}
