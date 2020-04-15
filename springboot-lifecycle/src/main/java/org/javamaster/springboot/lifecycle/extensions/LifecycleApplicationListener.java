package org.javamaster.springboot.lifecycle.extensions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author yudong
 * @date 2020/4/14
 */
@Slf4j
public class LifecycleApplicationListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        log.info("onApplicationEvent invoke:{}", event.getClass().getName());
    }

}
