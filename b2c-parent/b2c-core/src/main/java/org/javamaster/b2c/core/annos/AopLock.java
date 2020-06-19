package org.javamaster.b2c.core.annos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为方法加锁,处理完成再释放,若未提供spEL表达式,则默认以当前登录的用户名作为加锁的key
 *
 * @author yudong
 * @date 2019/7/11
 * @see org.javamaster.b2c.core.aspect.LockAspect
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AopLock {

    /**
     * spEL表达式,用于计算加锁的key
     */
    String lockKeySpEL() default "";

    /**
     * 加锁失败时的错误提示
     */
    String errorMsg() default "";

}
