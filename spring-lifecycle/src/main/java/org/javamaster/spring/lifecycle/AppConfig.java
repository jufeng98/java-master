package org.javamaster.spring.lifecycle;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javamaster.spring.lifecycle.DispatcherServletBeans.MvcContextBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 由于此类定义在了applicationContext.xml下, 因此会被ContextLoaderListener处理并纳入到ContextLoaderListener
 * 的ApplicationContext应用上下文管理
 * <p>
 * 又因为此类在component-scan的扫描路径下, 同时带有@Configuration注解, 所以也会被DispatcherServlet处理并纳入到
 * DispatcherServlet的ApplicationContext应用上下文管理, @Bean注解也是在这里处理的
 *
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
