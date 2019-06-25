package org.javamaster.b2c.classloader.service.impl;

import org.javamaster.b2c.classloader.service.HelloService;
import org.springframework.stereotype.Service;

/**
 * @author yudong
 * @date 2019/6/25
 */
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello() {
        return "hello world";
    }
}
