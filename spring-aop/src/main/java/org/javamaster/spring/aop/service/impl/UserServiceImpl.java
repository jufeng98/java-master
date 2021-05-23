package org.javamaster.spring.aop.service.impl;

import org.apache.commons.lang3.RandomUtils;
import org.javamaster.spring.aop.service.UserService;

/**
 * @author yudong
 * @date 2021/4/26
 */
public class UserServiceImpl implements UserService {

    @Override
    public Integer createUser(String username) {
        if (RandomUtils.nextBoolean()) {
            throw new RuntimeException("模拟错误发生");
        }
        return 1;
    }

}
