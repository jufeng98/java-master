package org.javamaster.spring.lifecycle.ContextLoaderListenerBeans;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 各个接口方法的执行顺序从上到下
 *
 * @author yudong
 * @date 2020/3/31
 */
@Slf4j
public class ContextBean implements BeanNameAware, BeanClassLoaderAware, BeanFactoryAware,
        InitializingBean, DisposableBean, ApplicationContextAware {

    private ContextBean1 contextBean1;

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

    /**
     * PostConstruct注解不被ContextLoaderListener的ApplicationContext支持
     * 而此类是定义在ContextLoaderListener的applicationContext.xml下,所以此方法不会被调用
     *
     * 不过,如果我们手工在applicationContext.xml注册CommonAnnotationBeanPostProcessor这个
     * bean,则PostConstruct注解就可以起作用了
     */
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

    public ContextBean1 getContextBean1() {
        return contextBean1;
    }

    public void setContextBean1(ContextBean1 contextBean1) {
        this.contextBean1 = contextBean1;
    }
}
