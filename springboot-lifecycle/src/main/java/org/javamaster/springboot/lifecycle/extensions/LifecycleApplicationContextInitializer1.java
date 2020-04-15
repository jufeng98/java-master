package org.javamaster.springboot.lifecycle.extensions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author yudong
 * @date 2020/4/14
 */
@Slf4j
public class LifecycleApplicationContextInitializer1 implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("initialize invoke:{}", applicationContext.getId());
    }

}
