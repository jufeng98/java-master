package org.javamaster.spring.tools.manage.util;

import org.apache.commons.lang3.StringUtils;
import org.javamaster.spring.tools.manage.model.ConnectionVo;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.javamaster.spring.tools.manage.service.impl.RedisServiceImpl.HASH_KEY_DBS;

/**
 * @author yudong
 * @date 2023/3/3
 */
@SuppressWarnings("unchecked")
public class RedisUtils {

    /**
     * 获取连接对象信息
     */
    public static ConnectionVo getConnectionVo(String connectId) {
        RedisTemplate<String, Object> redisTemplate = (RedisTemplate<String, Object>) SpringUtils.getContext()
                .getBean("redisTemplateJackson");
        return (ConnectionVo) redisTemplate.opsForHash().get(HASH_KEY_DBS, connectId);
    }

    /**
     * 获取对应的RedisTemplate对象
     */
    public static RedisTemplate<Object, Object> getRedisTemplate(String connectId, Integer db) {
        ConnectionVo connectionVo = getConnectionVo(connectId);
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext()
                .getAutowireCapableBeanFactory();
        String id = connectId + ":" + db;
        if (beanFactory.containsBean(id)) {
            return (RedisTemplate<Object, Object>) beanFactory.getBean(id);
        }
        JedisConnectionFactory connectionFactory = getJedisConnectionFactory(connectionVo, db);
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.afterPropertiesSet();
        // 将对象纳入到Spring中管理
        beanFactory.registerSingleton(id, redisTemplate);
        return redisTemplate;
    }

    public static JedisConnectionFactory getJedisConnectionFactory(ConnectionVo connectionVo, Integer db) {
        JedisConnectionFactory connectionFactory;
        if (StringUtils.isNotBlank(connectionVo.getNodes())) {
            connectionFactory = getJedisConnectionFactoryCluster(connectionVo.getNodes(), connectionVo.getPassword());
        } else {
            connectionFactory = getJedisConnectionFactorySingle(connectionVo.getHost(), connectionVo.getPort(),
                    db, connectionVo.getPassword());
        }
        connectionFactory.getConnection();
        return connectionFactory;
    }

    private static JedisConnectionFactory getJedisConnectionFactorySingle(String host, Integer port, Integer db, String pwd) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(db);
        configuration.setPassword(pwd);
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(configuration);
        jedisConnectionFactory.afterPropertiesSet();
        return jedisConnectionFactory;
    }

    public static Set<RedisNode> redisNodes(String nodes) {
        return Arrays.stream(nodes.split(","))
                .map(s -> {
                    String[] split = s.split(":");
                    return new RedisNode(split[0], Integer.parseInt(split[1]));
                })
                .collect(Collectors.toSet());
    }

    private static JedisConnectionFactory getJedisConnectionFactoryCluster(String nodes, String pwd) {
        Set<RedisNode> nodesSet = redisNodes(nodes);
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
        clusterConfiguration.setClusterNodes(nodesSet);
        clusterConfiguration.setPassword(pwd);
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(clusterConfiguration);
        jedisConnectionFactory.afterPropertiesSet();
        return jedisConnectionFactory;
    }

}
