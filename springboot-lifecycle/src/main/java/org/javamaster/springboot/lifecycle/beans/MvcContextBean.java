package org.javamaster.springboot.lifecycle.beans;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * bean的生命周期, 各个接口方法的执行顺序从上到下
 *
 * @author yudong
 * @date 2020/3/31
 */
@Slf4j
@Component
public class MvcContextBean implements BeanNameAware, BeanClassLoaderAware, BeanFactoryAware,
        InitializingBean, DisposableBean, ApplicationContextAware {

    @Autowired
    private ContextBean contextBean;
    @Value("${app.name}")
    private String appName;

    @Override
    public void setBeanName(String name) {
        log.info("setBeanName invoke:{}", name);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        log.info("setBeanClassLoader invoke:{}", classLoader.getClass().getName());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        log.info("setBeanFactory invoke:{}", beanFactory.getClass().getName());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("setApplicationContext invoke:{},{}", applicationContext.getClass().getName(), applicationContext.getId());
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct invoke");
    }

    @Override
    public void afterPropertiesSet() {
        log.info("afterPropertiesSet invoke");
    }

    public void initMethod() {
        log.info("initMethod invoke");
    }


    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy invoke");
    }

    @Override
    public void destroy() {
        log.info("destroy invoke");
    }


    public void destroyMethod() {
        log.info("destroyMethod invoke");
    }

}
