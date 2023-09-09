package org.javamaster.spring.refresh.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author yudong
 * @date 2022/9/9
 */
@Slf4j
@Configuration
@RefreshScope
public class DataSourceConfig {
    @Value("${jdbc.url}")
    private String url;
    @Value("${jdbc.username}")
    private String username;
    @Value("${jdbc.password}")
    private String password;
    @Value("${jdbc.maxPoolSize}")
    private Integer maxPoolSize;


    @Bean
    @RefreshScope
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        log.info("初始化数据源,maxPoolSize:{},对象地址:{}", maxPoolSize,
                dataSource.getClass().getName() + "@" + Integer.toHexString(dataSource.hashCode()));
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(maxPoolSize);
        return dataSource;
    }

    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource());
        return jdbcTemplate;
    }
}
