package org.javamaster.spring.tools.manage;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javamaster.spring.tools.manage.model.WebApiRspDto;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author yudong
 * @date 2023/3/4
 */
@RestControllerAdvice
@Slf4j
public class GlobalHandler {

    @ExceptionHandler(Exception.class)
    public WebApiRspDto<String> exceptionHandler(Exception e) {
        log.error("error", e);
        WebApiRspDto<String> webApiRspDto = WebApiRspDto.error(ExceptionUtils.getMessage(e) + "\r\n"
                + ExceptionUtils.getStackTrace(e));
        webApiRspDto.setMsg(ExceptionUtils.getMessage(e));
        return webApiRspDto;
    }

}
