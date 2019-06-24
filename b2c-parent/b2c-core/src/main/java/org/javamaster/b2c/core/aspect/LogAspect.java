package org.javamaster.b2c.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import static org.javamaster.b2c.core.utils.IpUtils.getIpAddr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;


/**
 * 统一日志记录
 *
 * @author yudong
 * @date 2019/6/10
 */
@Aspect
@Component
@Order(1)
public class LogAspect {

    private Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(public * org.javamaster.b2c.core.controller.*.*(..))")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long nowMillis = System.currentTimeMillis();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        Object[] parameters = joinPoint.getArgs();
        String classMethod = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        Object resObject;
        try {
            resObject = joinPoint.proceed(parameters);
        } catch (Exception e) {
            logger.error("aspect error req ip:{}|class_method:{}|args:{}", getIpAddr(request), classMethod, Arrays.toString(parameters));
            throw e;
        }
        logger.info("aspect req ip:{}|class_method:{}|args:{},response:{},cost time:{}ms", getIpAddr(request), classMethod,
                Arrays.toString(parameters), resObject, System.currentTimeMillis() - nowMillis);
        return resObject;
    }


}
