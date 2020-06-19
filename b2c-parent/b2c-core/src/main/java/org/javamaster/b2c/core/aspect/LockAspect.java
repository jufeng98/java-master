package org.javamaster.b2c.core.aspect;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.javamaster.b2c.core.annos.AopLock;
import org.javamaster.b2c.core.consts.AppConsts;
import org.javamaster.b2c.core.enums.BizExceptionEnum;
import org.javamaster.b2c.core.exception.BusinessException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


/**
 * @author yudong
 * @date 2019/7/11
 */
@Aspect
@Order(2)
@Component
public class LockAspect {

    @Autowired
    private RedissonClient redisson;
    @Autowired
    private ExpressionParser expressionParser;
    @Autowired
    private ParameterNameDiscoverer parameterNameDiscoverer;

    @Pointcut("@annotation(org.javamaster.b2c.core.annos.AopLock)")
    public void lockPointCut() {
    }

    @Around("lockPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        EvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        for (int i = 0; i < parameterNames.length; i++) {
            String paramName = parameterNames[i];
            context.setVariable(paramName, args[i]);
        }

        AopLock aopLock = method.getAnnotation(AopLock.class);
        String errorMsg = StringUtils.defaultString(aopLock.errorMsg(), BizExceptionEnum.OPERATION_TOO_FREQUENT.getErrorMsg());
        String spEl = aopLock.lockKeySpEL();
        String key = AppConsts.LOCK_KEY_PREFIX;
        if (StringUtils.isBlank(spEl)) {
            key += userDetails.getUsername();
        } else {
            key += (String) expressionParser.parseExpression(spEl).getValue(context);
        }
        RLock lock = redisson.getLock(key);
        try {
            boolean locked = lock.tryLock(3, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(BizExceptionEnum.OPERATION_TOO_FREQUENT.getErrorCode(), errorMsg);
            }
            return joinPoint.proceed(args);
        } finally {
            lock.unlock();
        }
    }


}
