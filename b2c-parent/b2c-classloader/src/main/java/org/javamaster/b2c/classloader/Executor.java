package org.javamaster.b2c.classloader;

import org.javamaster.b2c.classloader.service.HelloService;
import org.javamaster.b2c.classloader.utils.SpringUtils;

/**
 * @author yudong
 * @date 2019/6/25
 */
public class Executor {
    /**
     * 可在服务器上执行任意代码
     *
     * @return
     */
    public static Object execute() {
        // 这里代码可以随意修改,然后让服务器执行
        HelloService helloService = SpringUtils.getContext().getBean(HelloService.class);
        return helloService.sayHello();
    }
}
