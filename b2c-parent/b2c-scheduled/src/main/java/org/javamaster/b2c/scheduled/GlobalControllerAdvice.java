package org.javamaster.b2c.scheduled;

import org.javamaster.b2c.scheduled.consts.AppConsts;
import org.javamaster.b2c.scheduled.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常捕获处理
 *
 * @author yudong
 * @date 2019/5/10
 */
@RestControllerAdvice
public class GlobalControllerAdvice {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = IllegalArgumentException.class)
    public Result<Void> exceptionHandler(IllegalArgumentException e) {
        logger.error("argument error", e);
        return new Result<>(AppConsts.FAILED, e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public Result<Void> exceptionHandler(Exception e) {
        logger.error("application error", e);
        return new Result<>(AppConsts.FAILED, AppConsts.FAILED_MSG);
    }
}
