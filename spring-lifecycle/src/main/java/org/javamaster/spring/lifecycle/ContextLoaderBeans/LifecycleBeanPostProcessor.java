package org.javamaster.spring.lifecycle.ContextLoaderBeans;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 扩展点,可以定制bean, 使用Ordered接口或者PriorityOrdered接口指定多个都
 * 实现了BeanFactoryPostProcessor接口的类执行顺序
 * <p>
 * 由于此类定义在ContextLoaderListener的applicationContext.xml下, 所以只在此范围内会被调用
 *
 * @author yudong
 * @date 2020/3/31
 */
@Slf4j
public class LifecycleBeanPostProcessor implements BeanPostProcessor {

    private int i = 0;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (i++ == 0) {
            System.out.print("");
        }
        log.info("postProcessBeforeInitialization invoke:{},{}", bean.getClass().getName(), beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        log.info("postProcessAfterInitialization invoke:{},{}", bean.getClass().getName(), beanName);
        return bean;
    }
}
