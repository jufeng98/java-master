package org.javamaster.invocationlab.admin.service.invocation.impl;

import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam;
import org.javamaster.invocationlab.admin.service.invocation.AbstractInvoker;
import org.javamaster.invocationlab.admin.service.invocation.Invocation;
import org.javamaster.invocationlab.admin.service.invocation.Invoker;
import org.javamaster.invocationlab.admin.service.invocation.entity.DubboParamValue;
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils;
import lombok.SneakyThrows;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

import static org.javamaster.invocationlab.admin.consts.Constant.FEIGN_PARAM;

/**
 * @author yudong
 */
@Service
class FeignInvoker extends AbstractInvoker implements Invoker<Object, PostmanDubboRequest> {

    @SneakyThrows
    @Override
    public WebApiRspDto<Object> invoke(PostmanDubboRequest request, Invocation invocation) {
        String serviceKey = BuildUtils.buildServiceKey(request.getCluster(), request.getServiceName());
        GenericApplicationContext context = InvokeContext.getFeignContext(serviceKey);

        @SuppressWarnings("DataFlowIssue")
        Class<?> feignClz = context.getClassLoader().loadClass(request.getInterfaceName());
        Object feignService = context.getBean(feignClz);

        Class<?>[] parameterTypes = invocation.getParams().stream()
                .map(RequestParam::getTargetParaType)
                .toArray(Class[]::new);
        Method method = feignClz.getDeclaredMethod(invocation.getJavaMethodName(), parameterTypes);
        method.setAccessible(true);

        final DubboParamValue rpcParamValue = converter.convert(request, invocation);
        try {
            ThreadLocalUtils.set(FEIGN_PARAM, new Pair<>(request, rpcParamValue));
            Object res = method.invoke(feignService, rpcParamValue.getParamValues().toArray(new Object[0]));
            return WebApiRspDto.success(res);
        } finally {
            ThreadLocalUtils.remove(FEIGN_PARAM);
        }
    }

    @Override
    public WebApiRspDto<Object> invoke(Pair<PostmanDubboRequest, Invocation> pair) {
        return invoke(pair.getLeft(), pair.getRight());
    }

}
