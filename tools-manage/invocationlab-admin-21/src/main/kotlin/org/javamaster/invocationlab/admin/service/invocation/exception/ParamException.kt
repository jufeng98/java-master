package org.javamaster.invocationlab.admin.service.invocation.exception

import org.javamaster.invocationlab.admin.service.invocation.ResponseCode

/**
 * @author yudong
 * 参数解析异常
 */
class ParamException(msg: String?, val code: Int = ResponseCode.SYSTEM_ERROR.code) : Exception(msg)
