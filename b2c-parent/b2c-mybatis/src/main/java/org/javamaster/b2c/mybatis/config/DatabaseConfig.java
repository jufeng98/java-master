package org.javamaster.b2c.mybatis.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;


/**
 * @author yudong
 * @date 2019/9/1
 */
@Configuration
@MapperScan(basePackages = "org.javamaster.b2c.mybatis.mapper")
public class DatabaseConfig {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * mybatis配置的核心bean
     */
    @Bean
    public SqlSessionFactory mysqlSqlSessionFactory(DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        final String MAPPER_LOCATION = "classpath:mapper/**/*.xml";
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources(MAPPER_LOCATION));
        String configLocation = "classpath:mybatis-config.xml";
        sqlSessionFactoryBean.setConfigLocation(resolver.getResources(configLocation)[0]);
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
        return sqlSessionFactory;
    }

}
