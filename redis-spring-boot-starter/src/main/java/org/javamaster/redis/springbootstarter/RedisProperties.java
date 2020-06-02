package org.javamaster.redis.springbootstarter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author yudong
 * @date 2020/5/18
 */
@Data
@ConfigurationProperties(prefix = "javamaster.redis")
public class RedisProperties {

    private Single single;

    private Cluster cluster;

    private Pool pool = new Pool();

    @Data
    public static class Single {
        private String host;
        private String password;
        private int port;
    }

    @Data
    public static class Cluster {
        private List<String> nodes;
    }


    @Data
    public static class Pool {
        private int maxIdle = 8;
        private int minIdle = 0;
        private int maxActive = 8;
        private int maxWait = -1;
    }

}
