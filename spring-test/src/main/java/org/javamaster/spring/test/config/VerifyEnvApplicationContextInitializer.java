package org.javamaster.spring.test.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yudong
 * @date 2021/5/15
 */
@SuppressWarnings("NullableProblems")
public class VerifyEnvApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Set<String> ALLOW_ENV_SET = new HashSet<>();

    static {
        ALLOW_ENV_SET.add("dev");
        ALLOW_ENV_SET.add("test");
        ALLOW_ENV_SET.add("prd");
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // 校验环境配置
        String env = System.getProperty("env");
        if (env == null || "".equals(env)) {
            throw new IllegalStateException("环境属性未设置,请加上-Denv参数指定环境配置");
        }
        if (!ALLOW_ENV_SET.contains(env)) {
            throw new IllegalStateException("环境属性设置错误,只允许" + ALLOW_ENV_SET + "中的值,当前设置为:" + env);
        }
    }
}
