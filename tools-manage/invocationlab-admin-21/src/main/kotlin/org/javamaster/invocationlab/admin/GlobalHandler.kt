package org.javamaster.invocationlab.admin

import org.javamaster.invocationlab.admin.config.BizException
import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.model.ResultVo
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.util.ExceptionUtils.getSimplifyStackTrace
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody

/**
 * @author yudong
 * @date 2022/11/12
 */
@ControllerAdvice
class GlobalHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ResponseBody
    @ExceptionHandler(ErdException::class)
    fun exceptionHandler(e: ErdException): ResultVo<String> {
        val trace = getSimplifyStackTrace(e)
        logger.error("业务异常:$trace")
        return ResultVo.fail(e.code, e.message)
    }

    @ResponseBody
    @ExceptionHandler(BizException::class)
    fun exceptionHandler(e: BizException?): WebApiRspDto<String> {
        val trace = getSimplifyStackTrace(
            e!!
        )
        logger.error("业务异常:$trace")
        val webApiRspDto = WebApiRspDto.error<String>(trace)
        webApiRspDto.msg = trace
        return webApiRspDto
    }

    @ResponseBody
    @ExceptionHandler(Exception::class)
    fun exceptionHandler(e: Exception?): WebApiRspDto<String> {
        logger.error("系统异常", e)
        val webApiRspDto = WebApiRspDto.error<String>(ExceptionUtils.getStackTrace(e))
        webApiRspDto.msg = ExceptionUtils.getMessage(e)
        return webApiRspDto
    }
}
