package org.javamaster.b2c.core.annos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为方法加锁,处理完成再释放,防止短时间内重复调用可能导致的问题
 *
 * @author yudong
 * @date 2019/7/11
 * @see org.javamaster.b2c.core.aspect.LockAspect
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AopLock {
}
