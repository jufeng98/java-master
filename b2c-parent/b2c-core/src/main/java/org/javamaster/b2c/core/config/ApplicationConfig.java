package org.javamaster.b2c.core.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * @author yudong
 * @date 2019/7/11
 */
@Configuration
public class ApplicationConfig {

    @Value("${redisson.node.address}")
    private String address;

    @Value("${redisson.node.password}")
    private String password;


    @Bean
    public Config config() {
        Config config = new Config();
        config.useSingleServer().setAddress(address).setPassword(password);
        return config;
    }

    @Bean
    public RedissonClient redissonClient(Config config) {
        return Redisson.create(config);
    }

    @Bean
    public ExpressionParser expressionParser() {
        return new SpelExpressionParser();
    }

    @Bean
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        return new DefaultParameterNameDiscoverer();
    }

}
