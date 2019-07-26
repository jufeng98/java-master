package org.javamaster.mocklombok.javac.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成字段的get方法
 *
 * @author yudong
 * @date 2019/1/23
 * @see org.javamaster.mocklombok.javac.processor.GenerateGetMethodProcessor
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface GenerateGetMethod {
}
