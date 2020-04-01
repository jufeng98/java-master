package org.javamaster.spring.lifecycle.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 扩展点
 *
 * @author yudong
 * @date 2020/3/31
 */
@Slf4j
public class LifecycleApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * 会被调用两次,第一次调用当ContextLoaderListener的应用上下文ApplicationContext下所有的bean创建完成时
     * <p>
     * 第二次调用当DispatcherServlet的应用上下文ApplicationContext下所有的bean创建完成时
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = (ApplicationContext) event.getSource();
        log.info("onApplicationEvent invoke:{},{}", event.getClass().getName(), applicationContext.getId());
    }

}

