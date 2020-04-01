package org.javamaster.spring.lifecycle.ContextLoaderBeans;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;
import java.util.Map;

/**
 * 扩展点, 可以定制 Environment
 *
 * @author yudong
 * @date 2020/4/1
 */
@Slf4j
public class LifecycleEnvironmentAware implements EnvironmentAware {

    @Override
    public void setEnvironment(Environment environment) {
        MutablePropertySources mutablePropertySources = ((ConfigurableEnvironment) environment).getPropertySources();
        Map<String, Object> map = new HashMap<>(1, 1);
        map.put("welcome", "helloWorld");
        MapPropertySource mapPropertySource = new MapPropertySource("customizePropertySource", map);
        mutablePropertySources.addLast(mapPropertySource);
        log.info("setEnvironment invoke:{}", environment.getClass().getName());
    }

}
