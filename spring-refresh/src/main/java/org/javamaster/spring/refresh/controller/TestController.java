package org.javamaster.spring.refresh.controller;

import lombok.extern.slf4j.Slf4j;
import org.javamaster.spring.refresh.config.DataSourceSsmConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yudong
 * @date 2023/9/9
 */
@Slf4j
@RestController
@RefreshScope
@RequestMapping("/test")
public class TestController {
    @Value("${maxActive}")
    private Integer maxActive;
    @Autowired
    private ContextRefresher contextRefresher;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    @Qualifier("jdbcTemplateSsm")
    private JdbcTemplate jdbcTemplateSsm;

    @GetMapping("/getMaxActive")
    public Integer getMaxActive() {
        return maxActive;
    }

    @GetMapping("/refresh")
    public Set<String> refresh() {
        return contextRefresher.refresh();
    }

    @GetMapping("/queryDbTime")
    public String queryDbTime() {
        return jdbcTemplate.queryForObject("select current_timestamp", String.class);
    }

    @PostMapping("/refreshSsmDataSource")
    public Set<String> refreshSsmDataSource(String[] changeKeys) {
        Set<String> set = Arrays.stream(changeKeys).collect(Collectors.toSet());
        // 发布EnvironmentChangeEvent事件，通知Spring哪些属性发生了变化
        context.publishEvent(new EnvironmentChangeEvent(this.context, set));
        DataSourceSsmConfig dataSourceSsmConfig = context.getBean(DataSourceSsmConfig.class);
        // 刷新数据源对象
        dataSourceSsmConfig.refreshDatasource();
        return set;
    }

    @GetMapping("/queryDbTimeSsm")
    public String queryDbTimeSsm() {
        return jdbcTemplateSsm.queryForObject("select current_timestamp", String.class);
    }

}
