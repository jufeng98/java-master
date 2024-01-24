package org.javamaster.invocationlab.admin.util;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author yudong
 */
@Component
public class SpringUtils implements ApplicationContextAware {

    @Getter
    private static ApplicationContext context;
    @Setter
    @Getter
    private static boolean proEnv;
    @Setter
    @Getter
    private static boolean devEnv;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext ac) {
        SpringUtils.context = ac;
    }

}
