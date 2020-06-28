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

//        这种方式拿不到方法参数真正的名称,只会拿到arg0,arg1之类的无意义名称
//        for (Parameter parameter : method.getParameters()) {
//            String paramName = parameter.getName();
//        }

        EvaluationContext evaluationContext = new StandardEvaluationContext();
        // 这里Spring最终使用的是LocalVariableTableParameterNameDiscoverer类,它对ASM进行了封装,读取了Class文件的LocalVariableTable
        // 来得到方法参数的真正名称,当然这里需要编译器开启输出调试符号信息的参数的-g,生成的Class文件才会带有LocalVariableTable
        // 一般默认都会开启
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        for (int i = 0; i < parameterNames.length; i++) {
            String paramName = parameterNames[i];
            // 参数名称和参数对象设置到表达式上下文对象里,这样才能通过 #reqVo 这样的写法来引用方法参数
            evaluationContext.setVariable(paramName, args[i]);
        }

        AopLock aopLock = method.getAnnotation(AopLock.class);
        String errorMsg = StringUtils.defaultString(aopLock.errorMsg(), BizExceptionEnum.OPERATION_TOO_FREQUENT.getErrorMsg());
        String spEl = aopLock.lockKeySpEL();
        String key = AppConsts.LOCK_KEY_PREFIX;
        if (StringUtils.isBlank(spEl)) {
            key += userDetails.getUsername();
        } else {
            // 通过ExpressionParser类对象来解析SpEL表达式
            key += (String) expressionParser.parseExpression(spEl).getValue(evaluationContext);
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
