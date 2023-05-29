package org.javamaster.invocationlab.admin.util;

import org.javamaster.invocationlab.admin.model.redis.ConnectionVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.service.impl.RedisServiceImpl.HASH_KEY_DBS;

/**
 * @author yudong
 */
public class RedisUtils {

    public static ConnectionVo getConnectionVo(String connectId) {
        StringRedisTemplate stringRedisTemplate = SpringUtils.getContext().getBean(StringRedisTemplate.class);
        ObjectMapper objectMapper = SpringUtils.getContext().getBean(ObjectMapper.class);
        String jsonStr = (String) stringRedisTemplate.opsForHash().get(HASH_KEY_DBS, connectId);
        try {
            return objectMapper.readValue(jsonStr, ConnectionVo.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RedisTemplate<Object, Object> getRedisTemplate(String connectId, Integer db) {
        ConnectionVo connectionVo = getConnectionVo(connectId);
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext()
                .getAutowireCapableBeanFactory();
        String id = connectId + ":" + db;
        if (beanFactory.containsBean(id)) {
            //noinspection unchecked
            return (RedisTemplate<Object, Object>) beanFactory.getBean(id);
        }
        JedisConnectionFactory connectionFactory = getJedisConnectionFactory(connectionVo, db);
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.afterPropertiesSet();
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
