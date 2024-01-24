package org.javamaster.invocationlab.admin.redis;

import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.model.redis.ConnectionVo;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.creation.impl.JdkCreator;
import org.javamaster.invocationlab.admin.util.SerializationUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisDataException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.javamaster.invocationlab.admin.util.RedisUtils.redisNodes;
import static redis.clients.jedis.ScanParams.SCAN_POINTER_START_BINARY;

@Component
public class RedisHelper {

    public Pair<Integer, Map<Integer, Long>> getSingleDbs(Jedis jedis) {
        Map<Integer, Long> map = Maps.newHashMap();
        int dbCount = 0;
        while (true) {
            try {
                jedis.select(dbCount++);
                if (SpringUtils.isProEnv()) {
                    map.put(dbCount - 1, -1L);
                } else {
                    map.put(dbCount - 1, jedis.dbSize());
                }
            } catch (JedisDataException e) {
                dbCount--;
                break;
            }
        }
        jedis.select(Protocol.DEFAULT_DATABASE);
        return new Pair<>(dbCount, map);
    }

    public Pair<Integer, Map<Integer, Long>> getClusterDbs(JedisCluster jedisCluster) {
        Map<Integer, Long> map = Maps.newHashMap();
        int dbCount = 1;
        Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
        long total = 0;
        if (SpringUtils.isProEnv()) {
            total = -1L;
        } else {
            for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
                try (Jedis jedis = entry.getValue().getResource()) {
                    String replication = jedis.info("Replication");
                    if (replication.contains("role:master")) {
                        total += jedis.dbSize();
                    }
                }
            }
        }
        map.put(0, total);
        return new Pair<>(dbCount, map);
    }

    public List<Triple<String, String, Class<?>>> getSingleDbKeys(Jedis jedis, int count, String pattern) {
        List<Triple<String, String, Class<?>>> list = Lists.newArrayList();
        byte[] cursor = SCAN_POINTER_START_BINARY;
        ScanParams scanParams = new ScanParams();
        scanParams.count(count);
        scanParams.match(pattern);
        do {
            ScanResult<byte[]> scanResult = jedis.scan(cursor, scanParams);
            List<byte[]> resultBytesList = scanResult.getResult();
            for (byte[] resultBytes : resultBytesList) {
                if (SerializationUtils.isJdkSerialize(resultBytes)) {
                    String base64 = Base64Utils.encodeToString(resultBytes);
                    Pair<String, Class<?>> pair = SerializationUtils.dealJdkDeserialize(resultBytes);
                    list.add(Triple.of(pair.getLeft(), base64, pair.getRight()));
                } else {
                    String key = new String(resultBytes, StandardCharsets.UTF_8);
                    list.add(Triple.of(key, "", null));
                }
            }
            if (list.size() >= 100) {
                break;
            }
            cursor = scanResult.getCursorAsBytes();
        } while (!Arrays.equals(SCAN_POINTER_START_BINARY, cursor));
        return list;
    }

    public List<Triple<String, String, Class<?>>> getClusterDbKeys(JedisClusterConnection clusterConnection,
                                                                   ConnectionVo connectionVo, int count, String pattern) {
        List<Triple<String, String, Class<?>>> list = Lists.newArrayList();
        Set<RedisNode> clusterNodes = redisNodes(connectionVo.getNodes());
        ScanOptions scanOptions = ScanOptions.scanOptions().count(count).match(pattern).build();
        Set<String> keys = Sets.newHashSet();
        outer:
        for (RedisNode clusterNode : clusterNodes) {
            RedisClusterNode redisClusterNode = new RedisClusterNode(Objects.requireNonNull(clusterNode.getHost()),
                    Objects.requireNonNull(clusterNode.getPort()));
            try (Cursor<byte[]> cursor = clusterConnection.scan(redisClusterNode, scanOptions)) {
                while (cursor.hasNext()) {
                    byte[] resultBytes = cursor.next();
                    String base64 = "";
                    String key;
                    Class<?> clazz = Void.class;
                    if (SerializationUtils.isJdkSerialize(resultBytes)) {
                        base64 = Base64Utils.encodeToString(resultBytes);
                        Pair<String, Class<?>> pair = SerializationUtils.dealJdkDeserialize(resultBytes);
                        key = pair.getLeft();
                        clazz = pair.getRight();
                    } else {
                        key = new String(resultBytes, StandardCharsets.UTF_8);
                    }
                    if (!keys.contains(key)) {
                        list.add(Triple.of(key, base64, clazz));
                        keys.add(key);
                    }
                    if (list.size() >= 100) {
                        break outer;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    public Pair<byte[], Class<?>> convertKeyToBytes(String redisKey, String redisKeyBase64) {
        byte[] keyBytes;
        Class<?> keyClazz;
        if (StringUtils.isNotBlank(redisKeyBase64)) {
            keyBytes = Base64Utils.decodeFromString(redisKeyBase64);
            Pair<String, Class<?>> pair = SerializationUtils.dealJdkDeserialize(keyBytes);
            keyClazz = pair.getRight();
        } else {
            keyBytes = redisKey.getBytes(StandardCharsets.UTF_8);
            keyClazz = Void.class;
        }
        return Pair.of(keyBytes, keyClazz);
    }

    public Pair<byte[], Class<?>> convertKeyToBytes(String redisKey, Boolean redisKeyJdkSerialize) {
        byte[] keyBytes;
        Class<?> keyClazz;
        if (BooleanUtils.isTrue(redisKeyJdkSerialize)) {
            Pair<String, Class<?>> pair = resolveVal(redisKey);
            Object obj = SerializationUtils.convertValToObj(pair.getLeft(), pair.getRight());
            keyBytes = SerializationUtils.serialize(obj);
            keyClazz = pair.getRight();
        } else {
            keyBytes = redisKey.getBytes(StandardCharsets.UTF_8);
            keyClazz = Void.class;
        }
        return Pair.of(keyBytes, keyClazz);
    }

    public static Pair<String, Class<?>> resolveVal(String s) {
        try {
            String[] split = s.split("♣");
            if (split.length > 1) {
                Class<?> clazz = ClassUtils.getClass(JdkCreator.globalJdkClassLoader, split[1]);
                return Pair.of(split[0], clazz);
            }
            return Pair.of(s, String.class);
        } catch (ClassNotFoundException e) {
            throw new BizException(s + "类型不存在!!!");
        }
    }
}
