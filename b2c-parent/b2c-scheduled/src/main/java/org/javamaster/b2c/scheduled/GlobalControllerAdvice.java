package org.javamaster.b2c.scheduled;

import org.javamaster.b2c.scheduled.consts.AppConsts;
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

    @ExceptionHandler(value = Exception.class)
    public Integer handel(Exception e) {
        logger.error("application error", e);
        return AppConsts.FAILED;
    }
}
