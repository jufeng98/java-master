package org.javamaster.spring.test.config;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import org.javamaster.spring.test.listener.CustomizedDubboTestListener;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import static org.javamaster.spring.test.GeneralTestCode.PROFILE_UNIT_TEST;

/**
 * @author yudong
 * @date 2021/5/15
 */
@SuppressWarnings("all")
@TestConfiguration
@Profile(PROFILE_UNIT_TEST)
public class DubboTestConfig implements ApplicationContextAware {
    static {
        System.setProperty("dubbo.application.qos.enable", "false");
    }

    private ApplicationContext applicationContext;

    @Bean
    public ApplicationConfig applicationConfig() {
        String name = applicationContext.getEnvironment().getProperty("spring.test.dubbo.application.name");
        ApplicationConfig application = new ApplicationConfig();
        application.setName(name);
        return application;
    }

    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        String address = applicationContext.getEnvironment().getProperty("spring.test.dubbo.registry.address");
        registryConfig.setAddress(address);
        registryConfig.setTimeout(30000);
        return registryConfig;
    }

    @Bean(name = ReferenceAnnotationBeanPostProcessor.BEAN_NAME)
    public ReferenceAnnotationBeanPostProcessor referenceAnnotationBeanPostProcessor() {
        return new ReferenceAnnotationBeanPostProcessor();
    }

    @Bean
    public CustomizedDubboTestListener customizeDubboTestListener() {
        return new CustomizedDubboTestListener();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
