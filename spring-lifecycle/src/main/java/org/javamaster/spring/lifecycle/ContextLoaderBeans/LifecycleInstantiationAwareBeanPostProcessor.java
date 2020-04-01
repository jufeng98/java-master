package org.javamaster.spring.lifecycle.ContextLoaderBeans;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.beans.PropertyDescriptor;

/**
 * 扩展点, 可以定制bean的创建过程
 *
 * @author yudong
 * @date 2020/4/1
 */
@Slf4j
public class LifecycleInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

    private int i = 0;

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if (i++ == 0) {
            System.out.print("");
        }
        log.info("postProcessBeforeInstantiation invoke:{},{}", beanClass.getName(), beanName);
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        log.info("postProcessAfterInstantiation invoke:{},{}", bean.getClass().getName(), beanName);
        return true;
    }

    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        log.info("postProcessPropertyValues invoke:{},{},{},{}", pvs.getClass().getName(), pds.getClass().getName(), bean.getClass().getName(), beanName);
        return pvs;
    }

}
