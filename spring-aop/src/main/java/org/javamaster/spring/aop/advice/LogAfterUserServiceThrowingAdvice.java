package org.javamaster.spring.aop.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.ThrowsAdvice;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * UserService出异常的地方均记录下请求参数和异常信息
 *
 * @author yudong
 * @date 2021/4/26
 */
@Slf4j
public class LogAfterUserServiceThrowingAdvice implements ThrowsAdvice {

    public void afterThrowing(Method method, Object[] args, Object target, Exception ex) {
        String classMethod = target.getClass().getSimpleName() + "#" + method.getName();
        log.error("req class method:{}\nargs:{}\nerr:{}", classMethod, Arrays.toString(args), ex.getMessage());
    }

}
