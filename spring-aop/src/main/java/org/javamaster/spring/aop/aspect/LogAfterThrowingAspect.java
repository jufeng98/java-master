package org.javamaster.spring.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author yudong
 * @date 2021/4/26
 */
@Slf4j
@Aspect
@Component
public class LogAfterThrowingAspect {

    @AfterThrowing(value = "bean(*Impl)", throwing = "throwable")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable throwable) {
        String classMethod = joinPoint.getTarget().getClass().getSimpleName() + "#" + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.error("req:{}\nargs:{}\nerr:{}", classMethod, Arrays.toString(args), throwable.getMessage());
    }

}
