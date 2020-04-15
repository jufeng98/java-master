package org.javamaster.springboot.lifecycle;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javamaster.springboot.lifecycle.beans.MvcContextBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yudong
 * @date 2020/3/31
 */
@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean(initMethod = "initMethod", destroyMethod = "destroyMethod")
    public MvcContextBean notContextBean() {
        return new MvcContextBean();
    }

}
