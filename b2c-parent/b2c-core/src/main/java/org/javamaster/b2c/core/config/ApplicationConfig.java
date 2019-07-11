package org.javamaster.b2c.core.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }

}
