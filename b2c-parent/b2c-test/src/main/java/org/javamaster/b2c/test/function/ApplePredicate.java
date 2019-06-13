package org.javamaster.b2c.test.function;

import org.javamaster.b2c.test.model.Apple;

/**
 * @author yudong
 * @date 2019/6/11
 */
public interface ApplePredicate {
    boolean test(Apple apple);
}
