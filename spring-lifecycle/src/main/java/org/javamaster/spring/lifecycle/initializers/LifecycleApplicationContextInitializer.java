package org.javamaster.spring.lifecycle.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 扩展点,可以定制applicationContext, 使用Ordered接口(等价物@Order注解)或者PriorityOrdered接口(等价物@Priority)指定多个
 * 都实现了ApplicationContextInitializer接口的类执行顺序
 *
 * @author yudong
 * @date 2020/3/31
 */
@Slf4j
public class LifecycleApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * 会被调用两次,第一次调用当ContextLoaderListener的应用上下文ApplicationContext下所有的bean创建完成时
     * <p>
     * 第二次调用当DispatcherServlet的应用上下文ApplicationContext下所有的bean创建完成时
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("initialize invoke:{}", applicationContext.getId());
    }

}
