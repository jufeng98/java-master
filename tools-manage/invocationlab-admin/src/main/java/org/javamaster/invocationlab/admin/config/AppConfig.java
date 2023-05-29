package org.javamaster.invocationlab.admin.config;

import org.javamaster.invocationlab.admin.service.maven.Maven;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 启动的时候systemInit()加载需要的zk地址和已经创建的服务
 *
 * @author yudong
 */
@Configuration
public class AppConfig {

    @Value("${dubbo.api.jar.dir}")
    String apiJarPath;
    @Value("${nexus.url}")
    private String nexusPath;
    @Value("${nexus.url.releases}")
    private String nexusPathReleases;
    @Autowired
    private RedisRepository redisRepository;

    @Bean
    Maven mavenProcessor() {
        return new Maven(nexusPath, nexusPathReleases, apiJarPath);
    }

    @Bean
    Initializer initializer() throws Exception {
        Initializer initializer = new Initializer();
        //统一设置路径入口,其他地方通过System.getProperty获取
        System.setProperty(Constant.USER_HOME, apiJarPath);
        initializer.copySettingXml(apiJarPath);
        initializer.loadZkAddress(redisRepository);
        initializer.loadCreatedService(redisRepository, RedisKeys.RPC_MODEL_KEY);
        return initializer;
    }
}
