package org.javamaster.spring.test.listener;

import org.javamaster.spring.test.utils.DubboTestUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author yudong
 * @date 2021/5/15
 */
@SuppressWarnings("all")
public class CustomizedDubboTestListener implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent applicationEvent) {
        String[] references = applicationContext.getEnvironment()
                .getProperty("spring.test.dubbo.references", String[].class, new String[]{});
        for (String reference : references) {
            String[] split = reference.split("\\|");
            DubboTestUtils.changeDubboReferenceProperty(applicationContext, split[0], split[1], split[2]);
        }
    }

}
