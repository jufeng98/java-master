package org.javamaster.invocationlab.admin.service.invocation;

import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanRequest;

/**
 * @author yudong
 */
public interface Invoker<T, R extends PostmanRequest> {

    WebApiRspDto<T> invoke(R request, Invocation invocation);

    /**
     * 在js文件里面需要调用这个方法,方便进行场景测试
     */
    WebApiRspDto<T> invoke(Pair<R, Invocation> pair);
}
