package org.javamaster.spring.refresh.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * @author yudong
 * @date 2022/9/9
 */
@Slf4j
@Configuration
public class DataSourceSsmConfig {
    @Autowired
    private ApplicationContext context;
    private DataSourceSsmInvocationHandler dataSourceSsmInvocationHandler;

    public static class DataSourceSsmInvocationHandler implements InvocationHandler {
        private DataSource dataSource;

        public void setDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public DataSource getDataSource() {
            return dataSource;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(dataSource, args);
        }
    }

    private HikariDataSource createDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        Environment env = context.getEnvironment();
        String url = env.getProperty("jdbc.url");
        String username = env.getProperty("jdbc.username");
        String password = env.getProperty("jdbc.password");
        @SuppressWarnings("ConstantConditions")
        int maxPoolSize = env.getProperty("jdbc.maxPoolSize", Integer.class);
        log.info("初始化ssm数据源,maxPoolSize:{},对象地址:{}", maxPoolSize,
                dataSource.getClass().getName() + "@" + Integer.toHexString(dataSource.hashCode()));
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(maxPoolSize);
        return dataSource;
    }

    @Bean
    public DataSource dataSourceSsm() {
        HikariDataSource dataSource = createDataSource();

        dataSourceSsmInvocationHandler = new DataSourceSsmInvocationHandler();
        // 这里是实现实现数据源的热更新的关键
        dataSourceSsmInvocationHandler.setDataSource(dataSource);

        // 返回代理对象
        return (DataSource) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{DataSource.class}, dataSourceSsmInvocationHandler);
    }

    /**
     * 实现数据源的热更新,避免重启服务
     */
    public void refreshDatasource() {
        HikariDataSource dataSource = ((HikariDataSource) dataSourceSsmInvocationHandler.getDataSource());
        // 销毁旧数据源对象
        dataSource.close();

        // 重新创建新的数据源对象并赋值给InvocationHandler
        // 这样就实现了数据源的热更新
        HikariDataSource newDataSource = createDataSource();
        dataSourceSsmInvocationHandler.setDataSource(newDataSource);
        log.info("完成刷新数据源对象");
    }

    @Bean
    public JdbcTemplate jdbcTemplateSsm() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSourceSsm());
        return jdbcTemplate;
    }

}
