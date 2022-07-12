package org.javamaster.b2c.core.config;

import com.github.pagehelper.PageHelper;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.javamaster.b2c.core.enums.EnumBase;
import org.javamaster.b2c.core.typehandler.EnumBaseTypeHandler;
import org.javamaster.b2c.core.utils.ClassUtils;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yudong
 * @date 2019/6/10
 */
@Configuration
@MapperScan(basePackages = "org.javamaster.b2c.core.mapper")
public class DatabaseConfig {

    /**
     * 使用内嵌数据库
     */
    @Bean
    public DataSource h2DataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("sql-script/schema.sql")
                .addScript("sql-script/data.sql")
                .build();
    }

    @Bean
    @SuppressWarnings("ALL")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);

        //分页插件
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("reasonable", "true");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("returnPageInfo", "check");
        properties.setProperty("params", "count=countSql");
        properties.setProperty("dialect", "MySQL");
        pageHelper.setProperties(properties);
        sqlSessionFactoryBean.setPlugins(new Interceptor[]{pageHelper});

        final String mapperLocation = "classpath*:mapper/**/*.xml";
        final String configLocation = "classpath:mybatis-config.xml";
        sqlSessionFactoryBean.setMapperLocations(resourcePatternResolver.getResources(mapperLocation));
        // 只指定包名,则mybatis会自动为 JavaBean 注册一个小写字母开头的非完全限定的类名形式的别名
        sqlSessionFactoryBean.setTypeAliasesPackage("org.javamaster.b2c.core.entity");
        XMLConfigBuilder builder = new XMLConfigBuilder(resourcePatternResolver.getResources(configLocation)[0].getInputStream(),
                null, null);
        org.apache.ibatis.session.Configuration configuration = builder.getConfiguration();
        configuration.setLogImpl(Slf4jImpl.class);

        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        sqlSessionFactoryBean.setConfiguration(configuration);
        // 找到EnumBase接口所在的包下所有实现该接口的枚举类
        Set<Class<?>> set = ClassUtils.getAllClassesFromPackage(EnumBase.class.getPackage().getName())
                .stream()
                .filter(clz -> clz.isEnum() && EnumBase.class.isAssignableFrom(clz))
                .collect(Collectors.toSet());
        // 动态注册所有实现了EnumBase接口枚举类的类型转换器到Mybatis
        set.forEach(enumClass -> {
            EnumBaseTypeHandler handler = new EnumBaseTypeHandler(enumClass);
            typeHandlerRegistry.register(enumClass, JdbcType.TINYINT, handler);
            typeHandlerRegistry.register(enumClass, null, handler);
        });

        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
