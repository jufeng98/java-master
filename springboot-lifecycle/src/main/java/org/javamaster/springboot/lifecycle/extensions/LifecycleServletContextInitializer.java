package org.javamaster.springboot.lifecycle.extensions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author yudong
 * @date 2020/4/14
 */
@Slf4j
@Component
public class LifecycleServletContextInitializer implements ServletContextInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        log.info("onStartup invoke:{}", servletContext.getClass().getName());
    }
}
