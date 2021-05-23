package org.javamaster.spring.aop.config;

import org.javamaster.spring.aop.advice.LogAfterUserServiceThrowingAdvice;
import org.javamaster.spring.aop.advisor.UserServicePointcutAdvisor;
import org.javamaster.spring.aop.pointcut.UserServicePointcut;
import org.javamaster.spring.aop.service.UserService;
import org.javamaster.spring.aop.service.impl.UserServiceImpl;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yudong
 * @date 2021/4/26
 */
@Configuration
public class AopConfig {

    @Bean
    public LogAfterUserServiceThrowingAdvice logAfterControllerThrowingAdvice() {
        return new LogAfterUserServiceThrowingAdvice();
    }

    @Bean
    public UserServicePointcut userServicePointcut() {
        return new UserServicePointcut();
    }

    @Bean
    public UserServicePointcutAdvisor userServicePointcutAdvisor(UserServicePointcut pointcut,
                                                                 LogAfterUserServiceThrowingAdvice advice) {
        return new UserServicePointcutAdvisor(pointcut, advice);
    }

//    @Bean
//    public ProxyFactoryBean userServiceImpl() {
//        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
//        proxyFactoryBean.setInterfaces(UserService.class);
//        proxyFactoryBean.setInterceptorNames("userServicePointcutAdvisor");
//
//        UserService target = new UserServiceImpl();
//        proxyFactoryBean.setTarget(target);
//        return proxyFactoryBean;
//    }

    @Bean
    public UserService userServiceImpl() {
        return new UserServiceImpl();
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

}
