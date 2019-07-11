package org.javamaster.b2c.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.javamaster.b2c.core.enums.BizExceptionEnum;
import org.javamaster.b2c.core.exception.BizException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * @author yudong
 * @date 2019/7/11
 */
@Aspect
@Component
@Order(2)
public class LockAspect {

    @Autowired
    private RedissonClient redisson;

    @Pointcut("@annotation(org.javamaster.b2c.core.annos.AopLock)")
    public void lockPointCut() {
    }

    @Around("lockPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object resObject;
        Object[] args = joinPoint.getArgs();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RLock lock = redisson.getLock(userDetails.getUsername());
        try {
            boolean locked = lock.tryLock(3, TimeUnit.SECONDS);
            if (!locked) {
                throw new BizException(BizExceptionEnum.OPERATION_TOO_FREQUENT);
            }
            resObject = joinPoint.proceed(args);
        } finally {
            lock.unlock();
        }
        return resObject;
    }


}
