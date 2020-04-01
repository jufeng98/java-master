package org.javamaster.spring.lifecycle.ContextLoaderBeans;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 扩展点,可以定制BeanFactory,使用Ordered接口或者PriorityOrdered接口指定多个都
 * 实现了BeanFactoryPostProcessor接口的类执行顺序
 * <p>
 * 由于此类定义在ContextLoaderListener的applicationContext.xml下, 所以只在此范围内会被调用
 *
 * @author yudong
 * @date 2020/3/31
 */
@Slf4j
public class LifecycleBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info("postProcessBeanFactory invoke:{}", beanFactory.getClass().getName());
    }

}
