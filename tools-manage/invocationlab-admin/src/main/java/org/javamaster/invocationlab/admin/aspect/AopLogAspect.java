package org.javamaster.invocationlab.admin.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.impl.RedisServiceImpl;
import org.springframework.stereotype.Component;

import java.util.Arrays;


/**
 * @author yudong
 */
@Slf4j
@Aspect
@Component
public class AopLogAspect {

    /**
     * 记录方法的入参和出参
     */
    @Around("@annotation(org.javamaster.invocationlab.admin.annos.AopLog)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        TokenVo tokenVo = RedisServiceImpl.getTokenVo();
        Object[] parameters = joinPoint.getArgs();
        String classMethod = joinPoint.getSignature().getDeclaringType().getSimpleName() + "#" + joinPoint.getSignature().getName();
        log.info("aop req:{},args:{},user:{}", classMethod, Arrays.toString(parameters), tokenVo);
        Object resObj = joinPoint.proceed(parameters);
        log.info("aop req:{},res:{}", classMethod, resObj);
        return resObj;
    }

}
