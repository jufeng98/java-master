package org.javamaster.invocationlab.admin.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.ConnectionVo;
import org.javamaster.invocationlab.admin.model.redis.Tree;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.redis.RedisDataTypeService;
import org.javamaster.invocationlab.admin.redis.RedisHelper;
import org.javamaster.invocationlab.admin.redis.TripleFunction;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.RedisService;
import org.javamaster.invocationlab.admin.util.RedisUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Protocol;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.javamaster.invocationlab.admin.util.RedisUtils.getConnectionVo;

/**
 * @author yudong
 */
@Slf4j
@Service
public class RedisServiceImpl implements RedisService {
    public static final String HASH_KEY_DBS = "admin:erd:redis:dbs";
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplateJackson;
    @Autowired
    private Map<String, RedisDataTypeService> redisDataTypeServiceMap;

    @Override
    public String pingConnect(ConnectionVo connectionVoReq) {
        if (connectionVoReq.getHost().contains(",")) {
            connectionVoReq.setNodes(connectionVoReq.getHost());
            connectionVoReq.setHost("");
        }
        JedisConnectionFactory factory = RedisUtils.newJedisConnectionFactory(connectionVoReq, Protocol.DEFAULT_DATABASE);
        RedisConnection connection = null;
        try {
            connection = factory.getConnection();
        } finally {
            if (connection != null) {
                connection.close();
            }
            factory.destroy();
        }
        return connectionVoReq.getHost() + "连接成功";
    }

    @Override
    public String saveConnect(ConnectionVo connectionVoReq) {
        if (connectionVoReq.getHost().contains(",")) {
            connectionVoReq.setNodes(connectionVoReq.getHost());
            connectionVoReq.setHost("");
        }
        String connectId = RandomStringUtils.randomAlphabetic(12);
        connectionVoReq.setConnectId(connectId);
        connectionVoReq.setCreateTime(System.currentTimeMillis());
        redisTemplateJackson.opsForHash().put(HASH_KEY_DBS, connectId, connectionVoReq);
        return "保存成功";
    }

    @Override
    public List<ConnectionVo> listConnects() {
        List<Object> values = redisTemplateJackson.opsForHash().values(HASH_KEY_DBS);
        return values.stream()
                .map(obj -> {
                    ConnectionVo connectionVo = (ConnectionVo) obj;
                    connectionVo.setPassword("");
                    connectionVo.setUser("");
                    return connectionVo;
                })
                .sorted(Comparator.comparing(ConnectionVo::getCreateTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<Tree> listDb(String connectId) {
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.redisTemplateSingleton(connectId, Protocol.DEFAULT_DATABASE);
        return redisTemplate.execute((RedisCallback<List<Tree>>) connection -> {
            Object redisConnection = connection.getNativeConnection();
            Pair<Integer, Map<Integer, Long>> pair;
            if (redisConnection instanceof Jedis) {
                Jedis jedis = (Jedis) redisConnection;
                pair = redisHelper.getSingleDbs(jedis);
            } else {
                JedisCluster jedisCluster = (JedisCluster) redisConnection;
                pair = redisHelper.getClusterDbs(jedisCluster);
            }
            Integer dbCount = pair.getLeft();
            Map<Integer, Long> map = pair.getRight();
            return IntStream.range(0, dbCount)
                    .mapToObj(index -> {
                        Long keyCount = map.get(index);
                        return Tree.builder()
                                .redisDbIndex(index)
                                .keyCount(keyCount)
                                .isLeaf(false)
                                .label("DB" + index + "(" + keyCount + ")")
                                .build();
                    })
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<Tree> listKeys(String connectId, Integer redisDbIndex, String pattern) {
        int count = 10000;
        if (pattern.equals("*")) {
            count = 100;
        }
        int finalCount = count;
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.redisTemplateSingleton(connectId, redisDbIndex);
        List<Tree> resList = redisTemplate.execute((RedisCallback<List<Tree>>) connection -> {
            List<Tree> list = Lists.newArrayList();

            if (!pattern.contains("*") && !pattern.contains("?")) {
                byte[] patternBytes = pattern.getBytes(StandardCharsets.UTF_8);
                Boolean exists = connection.exists(patternBytes);
                if (Boolean.TRUE.equals(exists)) {
                    @SuppressWarnings("ConstantConditions")
                    String type = RedisUtils.handleType(connection.type(patternBytes).code());
                    Tree tree = Tree.builder()
                            .label(type + pattern)
                            .labelBase64("")
                            .typeLength(type.length())
                            .isLeaf(true)
                            .build();
                    list.add(tree);
                }
                return list;
            }

            Object nativeConnection = connection.getNativeConnection();
            if (nativeConnection instanceof Jedis) {
                Jedis jedis = (Jedis) nativeConnection;
                list = redisHelper.getSingleDbKeys(jedis, finalCount, pattern);
            } else {
                JedisClusterConnection clusterConnection = (JedisClusterConnection) RedisConnectionUtils
                        .getConnection(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
                ConnectionVo connectionVo = getConnectionVo(connectId);
                list = redisHelper.getClusterDbKeys(clusterConnection, connectionVo, finalCount, pattern);
            }

            return list;
        });

        //noinspection ConstantConditions
        if (resList.size() > 200) {
            resList = resList.subList(0, 200);
        }

        resList.sort(Comparator.comparing(Tree::getLabel));

        return resList;
    }

    @Override
    public ValueVo getValue(CommonRedisVo commonRedisVo) throws Exception {
        return executeCommand(commonRedisVo, (connection, keyPair, redisDataTypeService)
                -> redisDataTypeService.getValue(connection, keyPair));
    }

    @Override
    public ValueVo saveValue(CommonRedisVo commonRedisVo) throws Exception {
        TokenVo tokenVo = getTokenVo();
        log.info("{}-{} save value:{}", tokenVo.getUserId(), tokenVo.getUsername(), commonRedisVo);

        return executeCommand(commonRedisVo, (connection, keyPair, redisDataTypeService) -> {
            Long aLong = redisDataTypeService.saveValue(connection, keyPair, commonRedisVo);
            log.info("保存结果:{}", aLong);
            return commonRedisVo;
        });
    }

    @Override
    public String delField(CommonRedisVo commonRedisVo) throws Exception {
        TokenVo tokenVo = getTokenVo();
        log.info("{}-{} del field:{}", tokenVo.getUserId(), tokenVo.getUsername(), commonRedisVo);

        return executeCommand(commonRedisVo, (connection, keyPair, redisDataTypeService) -> {
            Long affect = redisDataTypeService.delField(connection, keyPair, commonRedisVo);
            return "删除成功:" + affect;
        });
    }

    @Override
    public String addField(CommonRedisVo commonRedisVo) throws Exception {
        TokenVo tokenVo = getTokenVo();
        log.info("{}-{} add field:{}", tokenVo.getUserId(), tokenVo.getUsername(), commonRedisVo);

        return executeCommand(commonRedisVo, (connection, keyPair, redisDataTypeService) -> {
            String affect = redisDataTypeService.addField(connection, keyPair, commonRedisVo);
            return "新增成功:" + affect;
        });
    }

    @Override
    public String setNewTtl(CommonRedisVo commonRedisVo) {
        TokenVo tokenVo = getTokenVo();
        log.info("{}-{} set new ttl:{}", tokenVo.getUserId(), tokenVo.getUsername(), commonRedisVo);

        return executeCommand(commonRedisVo, (connection, keyPair, redisDataTypeService) -> {
            Boolean res = redisDataTypeService.setTtlIfNecessary(connection, keyPair, commonRedisVo.getRedisKeyTtl());
            return "设置结果:" + res;
        });
    }

    @Override
    public ValueVo addKey(CommonRedisVo commonRedisVo) {
        TokenVo tokenVo = getTokenVo();
        log.info("{}-{} add key:{}", tokenVo.getUserId(), tokenVo.getUsername(), commonRedisVo);

        RedisTemplate<Object, Object> redisTemplate = RedisUtils.redisTemplateSingleton(commonRedisVo.getConnectId(),
                commonRedisVo.getRedisDbIndex());

        Pair<byte[], Class<?>> keyPair = redisHelper.convertKeyToBytes(commonRedisVo.getRedisKey(),
                commonRedisVo.getRedisKeyJdkSerialize());
        byte[] keyBytes = keyPair.getLeft();

        return redisTemplate.execute((RedisCallback<ValueVo>) connection -> {
            if (Boolean.TRUE.equals(connection.keyCommands().exists(keyBytes))) {
                throw new BizException("Redis key: " + commonRedisVo.getRedisKey() + " 已存在!!!");
            }

            DataType dataType = DataType.fromCode(commonRedisVo.getRedisKeyType());
            RedisDataTypeService redisDataTypeService = redisDataTypeServiceMap.get(dataType.code());
            ValueVo valueVo = redisDataTypeService.addKey(connection, keyPair, commonRedisVo);

            redisDataTypeService.setTtlIfNecessary(connection, keyPair, commonRedisVo.getRedisKeyTtl());

            return valueVo;
        });
    }

    @Override
    public String delKey(CommonRedisVo commonRedisVo) {
        TokenVo tokenVo = getTokenVo();
        log.info("{}-{} del key:{}", tokenVo.getUserId(), tokenVo.getUsername(), commonRedisVo);

        return executeCommand(commonRedisVo, (connection, keyPair, redisDataTypeService) ->
                "删除结果:" + redisDataTypeService.delKey(connection, keyPair));
    }

    @Override
    public ValueVo renameKey(CommonRedisVo commonRedisVo) {
        TokenVo tokenVo = getTokenVo();
        log.info("{}-{} rename key:{}", tokenVo.getUserId(), tokenVo.getUsername(), commonRedisVo);

        RedisTemplate<Object, Object> redisTemplate = RedisUtils.redisTemplateSingleton(commonRedisVo.getConnectId(),
                commonRedisVo.getRedisDbIndex());

        Pair<byte[], Class<?>> keyPair = redisHelper.convertKeyToBytes(commonRedisVo.getRedisKey(),
                commonRedisVo.getRedisKeyJdkSerialize());
        Pair<byte[], Class<?>> oldKeyPair = redisHelper.convertKeyToBytes(commonRedisVo.getOldRedisKey(),
                commonRedisVo.getRedisKeyBase64());

        return redisTemplate.execute((RedisCallback<ValueVo>) connection -> {
            DataType dataType = connection.keyCommands().type(oldKeyPair.getLeft());
            if (dataType == null || dataType == DataType.NONE) {
                throw new BizException("Redis key: " + commonRedisVo.getOldRedisKey() + " 已不存在!!!");
            }

            RedisDataTypeService redisDataTypeService = redisDataTypeServiceMap.get(dataType.code());
            return redisDataTypeService.renameKey(connection, oldKeyPair, keyPair, commonRedisVo);
        });
    }

    public <U> U executeCommand(CommonRedisVo commonRedisVo,
                                TripleFunction<RedisConnection, Pair<byte[], Class<?>>, RedisDataTypeService, U> function) {
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.redisTemplateSingleton(commonRedisVo.getConnectId(),
                commonRedisVo.getRedisDbIndex());

        RedisHelper redisHelper = SpringUtils.getContext().getBean(RedisHelper.class);
        Pair<byte[], Class<?>> keyPair = redisHelper.convertKeyToBytes(commonRedisVo.getRedisKey(),
                commonRedisVo.getRedisKeyBase64());
        byte[] keyBytes = keyPair.getLeft();

        return redisTemplate.execute((RedisCallback<U>) connection -> {
            DataType dataType = connection.keyCommands().type(keyBytes);
            if (dataType == null || dataType == DataType.NONE) {
                throw new BizException("Redis key: " + commonRedisVo.getRedisKey() + " 已不存在!!!");
            }

            RedisDataTypeService redisDataTypeService = redisDataTypeServiceMap.get(dataType.code());
            return function.apply(connection, keyPair, redisDataTypeService);
        });
    }

    public static TokenVo getTokenVo() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        @SuppressWarnings("ConstantConditions")
        HttpSession session = requestAttributes.getRequest().getSession();
        TokenVo tokenVo = (TokenVo) session.getAttribute("tokenVo");
        if (tokenVo == null) {
            tokenVo = new TokenVo();
            tokenVo.setUserId("anonymous");
            tokenVo.setUsername("匿名");
        }
        return tokenVo;
    }
}
