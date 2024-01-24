package org.javamaster.invocationlab.admin.service.scenetest;

import org.javamaster.invocationlab.admin.model.dto.UserCaseDto;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.invocation.Invocation;
import org.javamaster.invocationlab.admin.service.invocation.Invoker;
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 批量请求,用于关联测试的操作
 * 接口1传递参数给接口2
 *
 * @author yudong
 */
@Service
public class SceneTester {
    @Autowired
    Invoker<Object, PostmanDubboRequest> invoker;

    public Map<String, Object> process(List<UserCaseDto> caseDtoList, String sceneScript) {
        List<Pair<PostmanDubboRequest, Invocation>> requestList = buildRequest(caseDtoList);
        return JSEngine.runScript(requestList, invoker, sceneScript);
    }

    private List<Pair<PostmanDubboRequest, Invocation>> buildRequest(List<UserCaseDto> caseDtoList) {
        List<Pair<PostmanDubboRequest, Invocation>> requestList = new ArrayList<>(1);
        for (UserCaseDto caseDto : caseDtoList) {
            Pair<PostmanDubboRequest, Invocation> pair = InvokeContext.buildInvocation(
                    caseDto.getZkAddress(),
                    caseDto.getServiceName(),
                    caseDto.getInterfaceKey(),
                    caseDto.getMethodName(),
                    caseDto.getRequestValue(),
                    "");
            requestList.add(pair);
        }
        return requestList;
    }
}
