package org.javamaster.spring.file;

import lombok.extern.slf4j.Slf4j;
import org.javamaster.spring.file.model.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @author yudong
 * @date 2021/2/8
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> exceptionHandler(IllegalArgumentException e) {
        log.error("wrong", e);
        return new Result<>(1, e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(Exception e) {
        log.error("error", e);
        return new Result<>(-1, "系统出了点问题,请稍后再试");
    }

}
