package org.javamaster.springboot.lifecycle.extensions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author yudong
 * @date 2020/4/14
 */
@Slf4j
public class LifecycleSpringApplicationRunListener implements SpringApplicationRunListener {

    public LifecycleSpringApplicationRunListener(SpringApplication application, String[] args) {
        log.info("LifecycleSpringApplicationRunListener construct invoke:{},{}", application.getClass().getName(), args);
    }

    @Override
    public void starting() {
        log.info("starting invoke");
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        log.info("environmentPrepared invoke:{}", environment.getClass().getName());
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        log.info("contextPrepared invoke:{}", context.getClass().getName());
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        log.info("contextLoaded invoke:{}", context.getClass().getName());
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        log.info("started invoke:{}", context.getClass().getName());
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        log.info("running invoke:{}", context.getClass().getName());

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        log.info("failed invoke:{}", context.getClass().getName());
    }
}
