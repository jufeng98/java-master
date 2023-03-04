package org.javamaster.spring.tools.manage.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author yudong
 * @date 2023/3/3
 */
@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) {
        SpringUtils.context = ac;
    }

}
