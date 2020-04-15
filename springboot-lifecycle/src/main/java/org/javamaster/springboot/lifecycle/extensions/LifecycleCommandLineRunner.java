package org.javamaster.springboot.lifecycle.extensions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author yudong
 * @date 2020/4/15
 */
@Slf4j
@Component
public class LifecycleCommandLineRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        log.info("run invoke:{}", args);
    }

}
