package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.enums.RegisterCenterType;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.AppFactory;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.invocation.Invocation;
import org.javamaster.invocationlab.admin.service.invocation.Invoker;
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.javamaster.invocationlab.admin.service.impl.RedisServiceImpl.getTokenVo;

/**
 * 访问dubbo的对外接口
 *
 * @author yudong
 */
@Slf4j
@Controller
public class RpcPostmanProxyController {

    private static final Logger logger = LoggerFactory.getLogger(RpcPostmanProxyController.class);
    @Autowired
    private AppFactory appFactory;

    @SneakyThrows
    @RequestMapping(value = "/dubbo", method = RequestMethod.POST)
    @ResponseBody
    public Object dubbo(@RequestBody JsonNode jsonNode) {
        TokenVo tokenVo = getTokenVo();
        log.info("{}-{} request dubbo or eureka:{}", tokenVo.getUserId(), tokenVo.getUsername(), jsonNode);
        String cluster = jsonNode.get("cluster").asText();
        String serviceName = jsonNode.get("serviceName").asText();
        String interfaceKey = jsonNode.get("interfaceKey").asText();
        String methodName = jsonNode.get("methodName").asText();
        String dubboParam = jsonNode.get("dubboParam").asText();
        String dubboIp = jsonNode.get("dubboIp").asText();

        RegisterCenterType type = appFactory.getType(cluster);
        String serviceKey = BuildUtils.buildServiceKey(cluster, serviceName);
        PostmanService postmanService = InvokeContext.getService(serviceKey);
        InvokeContext.tryLoadRuntimeInfo(postmanService, type, serviceKey);

        Pair<PostmanDubboRequest, Invocation> pair = InvokeContext.buildInvocation(cluster, serviceName, interfaceKey,
                methodName, dubboParam, dubboIp);
        PostmanDubboRequest request = pair.getLeft();
        Invocation invocation = pair.getRight();
        if (logger.isDebugEnabled()) {
            logger.debug("接收RPC-POSTMAN请求:" + JsonUtils.objectToString(request));
        }
        Invoker<Object, PostmanDubboRequest> invoker = appFactory.getInvoker(cluster);
        return invoker.invoke(request, invocation).getData();
    }
}
