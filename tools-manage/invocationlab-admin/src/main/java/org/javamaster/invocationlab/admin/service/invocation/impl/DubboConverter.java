package org.javamaster.invocationlab.admin.service.invocation.impl;

import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam;
import org.javamaster.invocationlab.admin.service.invocation.Converter;
import org.javamaster.invocationlab.admin.service.invocation.Invocation;
import org.javamaster.invocationlab.admin.service.invocation.entity.DubboParamValue;
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest;
import org.javamaster.invocationlab.admin.service.invocation.exception.ParamException;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author yudong
 */
@Component
class DubboConverter implements Converter<PostmanDubboRequest, DubboParamValue> {

    private static final Logger logger = LoggerFactory.getLogger(DubboConverter.class);

    @Override
    public DubboParamValue convert(PostmanDubboRequest request, Invocation invocation) throws ParamException {
        @SuppressWarnings("unchecked")
        Map<String, Object> bodyMap = JsonUtils.parseObject(request.getDubboParam(), Map.class);
        if (bodyMap == null) {
            throw new ParamException("请求参数不能为空");
        }
        DubboParamValue rpcParamValue = new DubboParamValue();
        //遍历模板的参数名称
        for (RequestParam param : invocation.getParams()) {
            String paramName = param.getParaName();
            Class<?> targetType = param.getTargetParaType();
            boolean nullParam = false;
            Object paramValue = null;
            Object value = bodyMap.get(paramName);
            if (value == null) {
                //传入null的参数
                nullParam = true;
            } else {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(targetType.getClassLoader());
                try {
                    paramValue = JsonUtils.mapper.convertValue(value, targetType);
                } catch (Exception exp) {
                    logger.error("参数反序列化失败:" + value + "," + targetType, exp);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
            if (!nullParam && paramValue == null) {
                throw new ParamException("参数匹配错误,参数名称:" + paramName + ",请检查类型,参数类型:" + targetType.getName());
            }
            rpcParamValue.addParamTypeName(targetType.getName());
            rpcParamValue.addParamValue(paramValue);
        }
        parseExternalParams(request, rpcParamValue);
        return rpcParamValue;
    }

    private void parseExternalParams(PostmanDubboRequest request, DubboParamValue rpcParamValue) {
        if (request.getDubboIp() != null && !request.getDubboIp().isEmpty()) {
            String dubboIp = request.getDubboIp();
            rpcParamValue.setDubboUrl(rpcParamValue.getDubboUrl().replace("ip", dubboIp));
            rpcParamValue.setUseDubbo(true);
        }
        if (request.getCluster() != null) {
            String zk = request.getCluster();
            String accessZk = BuildUtils.buildZkUrl(zk);
            rpcParamValue.setRegistry(accessZk);
        }
    }
}
