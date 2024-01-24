package org.javamaster.invocationlab.admin;

import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.model.ResultVo;
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author yudong
 * @date 2022/11/12
 */
@ControllerAdvice
public class GlobalHandler {
    private final Logger logger = Logger.getLogger(getClass());

    @ResponseBody
    @ExceptionHandler(ErdException.class)
    public ResultVo<String> exceptionHandler(ErdException e) {
        String trace = org.javamaster.invocationlab.admin.util.ExceptionUtils.getSimplifyStackTrace(e);
        logger.error("业务异常:" + trace);
        return ResultVo.fail(e.getCode(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(BizException.class)
    public WebApiRspDto<String> exceptionHandler(BizException e) {
        String trace = org.javamaster.invocationlab.admin.util.ExceptionUtils.getSimplifyStackTrace(e);
        logger.error("业务异常:" + trace);
        WebApiRspDto<String> webApiRspDto = WebApiRspDto.error(trace);
        webApiRspDto.setMsg(trace);
        return webApiRspDto;
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public WebApiRspDto<String> exceptionHandler(Exception e) {
        logger.error("系统异常", e);
        WebApiRspDto<String> webApiRspDto = WebApiRspDto.error(ExceptionUtils.getStackTrace(e));
        webApiRspDto.setMsg(ExceptionUtils.getMessage(e));
        return webApiRspDto;
    }

}
