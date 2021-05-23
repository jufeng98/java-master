package org.javamaster.spring.aop.pointcut;

import org.javamaster.spring.aop.service.impl.UserServiceImpl;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

import java.lang.reflect.Method;

/**
 * @author yudong
 * @date 2021/4/26
 */
@SuppressWarnings("NullableProblems")
public class UserServicePointcut implements Pointcut {

    @Override
    public ClassFilter getClassFilter() {
        // 只匹配UserServiceImpl类
        return new ClassFilter() {
            @Override
            public boolean matches(Class<?> clazz) {
                return clazz == UserServiceImpl.class;
            }
        };
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return new MethodMatcher() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                // 匹配所有方法
                return true;
            }

            @Override
            public boolean isRuntime() {
                return false;
            }

            @Override
            public boolean matches(Method method, Class<?> targetClass, Object... args) {
                return false;
            }
        };
    }
}
