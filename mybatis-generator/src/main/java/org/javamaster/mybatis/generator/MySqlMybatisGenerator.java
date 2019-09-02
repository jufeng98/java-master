package org.javamaster.mybatis.generator;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 生成mybatis model,mapper等文件
 *
 * @author yudong
 * @date 2019/9/1
 */
public class MySqlMybatisGenerator {

    static Logger logger = LoggerFactory.getLogger(MySqlMybatisGenerator.class);

    public static void main(String[] args) {
        try {
            generator();
        } catch (Exception e) {
            logger.error("generated error", e);
        }
    }

    public static void generator() throws Exception {
        List<String> warnings = new ArrayList<String>();
        File propFile = ResourceUtils.getFile("classpath:generatorConfig.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(propFile));
        File configFile = ResourceUtils.getFile("classpath:generatorConfig.xml");
        ConfigurationParser cp = new ConfigurationParser(properties, warnings);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        logger.info("generated warnings:" + warnings);
    }

}
