package org.javamaster.spring.aop.advisor;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * @author yudong
 * @date 2021/5/23
 */
public class UserServicePointcutAdvisor implements PointcutAdvisor {
    private final Pointcut pointcut;
    private final Advice advice;

    public UserServicePointcutAdvisor(Pointcut pointcut, Advice advice) {
        this.pointcut = pointcut;
        this.advice = advice;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public boolean isPerInstance() {
        return true;
    }
}
