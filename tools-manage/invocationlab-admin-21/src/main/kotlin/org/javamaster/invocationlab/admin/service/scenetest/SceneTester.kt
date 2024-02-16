package org.javamaster.invocationlab.admin.service.scenetest

import org.javamaster.invocationlab.admin.model.dto.UserCaseDto
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.context.InvokeContext.buildInvocation
import org.javamaster.invocationlab.admin.service.invocation.Invocation
import org.javamaster.invocationlab.admin.service.invocation.Invoker
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * 批量请求,用于关联测试的操作
 * 接口1传递参数给接口2
 *
 * @author yudong
 */
@Service
class SceneTester {
    @Autowired
    lateinit var invoker: Invoker<Any, PostmanDubboRequest>

    fun process(caseDtoList: List<UserCaseDto>, sceneScript: String): Map<String, Any> {
        val requestList = buildRequest(caseDtoList)
        return JSEngine.runScript(requestList, invoker, sceneScript)
    }

    private fun buildRequest(caseDtoList: List<UserCaseDto>): List<Pair<PostmanDubboRequest, Invocation>> {
        val requestList: MutableList<Pair<PostmanDubboRequest, Invocation>> = ArrayList(1)
        for (caseDto in caseDtoList) {
            val pair = buildInvocation(
                caseDto.zkAddress,
                caseDto.serviceName,
                caseDto.interfaceKey,
                caseDto.methodName,
                caseDto.requestValue,
                ""
            )
            requestList.add(pair)
        }
        return requestList
    }
}
