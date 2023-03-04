package org.javamaster.spring.tools.manage.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.javamaster.spring.tools.manage.model.CommonVo;
import org.javamaster.spring.tools.manage.model.ConnectionVo;
import org.javamaster.spring.tools.manage.model.FieldVo;
import org.javamaster.spring.tools.manage.model.Tree;
import org.javamaster.spring.tools.manage.model.ValueVo;
import org.javamaster.spring.tools.manage.service.RedisService;
import org.javamaster.spring.tools.manage.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.javamaster.spring.tools.manage.util.RedisUtils.getConnectionVo;
import static org.javamaster.spring.tools.manage.util.RedisUtils.redisNodes;
import static redis.clients.jedis.Protocol.DEFAULT_DATABASE;
import static redis.clients.jedis.params.ScanParams.SCAN_POINTER_START_BINARY;

/**
 * @author yudong
 * @date 2023/3/3
 */
@Service
public class RedisServiceImpl implements RedisService {
    public static final String HASH_KEY_DBS = "tools-manage:redis:dbs";
    @Autowired
    private RedisTemplate<String, Object> redisTemplateJackson;

    @Override
    public String saveConnect(ConnectionVo connectionVoReq) {
        if (connectionVoReq.getHost().contains(",")) {
            connectionVoReq.setNodes(connectionVoReq.getHost());
            connectionVoReq.setHost("");
        }
        String connectId = RandomStringUtils.randomAlphabetic(12);
        connectionVoReq.setConnectId(connectId);
        redisTemplateJackson.opsForHash().put(HASH_KEY_DBS, connectId, connectionVoReq);
        return "保存成功";
    }

    @Override
    public List<ConnectionVo> listConnects() {
        List<Object> values = redisTemplateJackson.opsForHash().values(HASH_KEY_DBS);
        return values.stream().map(obj -> {
            ConnectionVo connectionVo = (ConnectionVo) obj;
            connectionVo.setPassword("");
            connectionVo.setUser("");
            return connectionVo;
        }).collect(Collectors.toList());
    }


    @Override
    public List<Tree> listDb(String connectId) {
        RedisConnectionFactory factory = RedisUtils.getRedisTemplate(connectId, DEFAULT_DATABASE)
                .getConnectionFactory();
        RedisConnection redisConnection = Objects.requireNonNull(factory).getConnection();
        int dbCount = 0;
        Map<Integer, Long> map = Maps.newHashMap();
        if (redisConnection instanceof JedisConnection jedisConnection) {
            try (Jedis jedis = jedisConnection.getJedis()) {
                while (true) {
                    try {
                        jedis.select(dbCount++);
                        map.put(dbCount - 1, jedis.dbSize());
                    } catch (JedisDataException e) {
                        dbCount--;
                        break;
                    }
                }
                jedis.select(DEFAULT_DATABASE);
            }
        } else {
            map.put(0, -1L);
            dbCount = 1;
        }
        return IntStream.range(0, dbCount)
                .mapToObj(index -> Tree.builder()
                        .redisDbIndex(index)
                        .keyCount(-1L)
                        .isLeaf(false)
                        .label("DB" + index + "(" + map.get(index) + ")")
                        .build())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    @Override
    public List<Tree> listKeys(String connectId, Integer redisDbIndex, String pattern) {
        int count = 10000;
        if (pattern.equals("*")) {
            // 限制返回的key数量
            count = 100;
        }
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.getRedisTemplate(connectId, redisDbIndex);
        int finalCount = count;
        List<Pair<String, String>> resList = redisTemplate.execute((RedisCallback<List<Pair<String, String>>>) connection -> {
            Object nativeConnection = connection.getNativeConnection();
            List<Pair<String, String>> list = Lists.newArrayList();
            if (nativeConnection instanceof Jedis jedis) {
                byte[] cursor = SCAN_POINTER_START_BINARY;
                ScanParams scanParams = new ScanParams();
                scanParams.count(finalCount);
                scanParams.match(pattern);
                do {
                    ScanResult<byte[]> scanResult = jedis.scan(cursor, scanParams);
                    List<byte[]> resultBytesList = scanResult.getResult();
                    for (byte[] resultBytes : resultBytesList) {
                        String base64 = "";
                        if (resultBytes[0] <= 0) {
                            // 可能是 JDK 序列化方式的key
                            base64 = Base64.getEncoder().encodeToString(resultBytes);
                        }
                        list.add(Pair.of(new String(resultBytes, StandardCharsets.UTF_8), base64));
                    }
                    if (list.size() >= 100) {
                        break;
                    }
                    cursor = scanResult.getCursorAsBytes();
                } while (!Arrays.equals(SCAN_POINTER_START_BINARY, cursor));
            } else {
                JedisClusterConnection clusterConnection = (JedisClusterConnection) RedisConnectionUtils
                        .getConnection(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
                ConnectionVo connectionVo = getConnectionVo(connectId);
                Set<RedisNode> clusterNodes = redisNodes(connectionVo.getNodes());
                ScanOptions scanOptions = ScanOptions.scanOptions().count(finalCount).match(pattern).build();
                outer:
                for (RedisNode clusterNode : clusterNodes) {
                    RedisClusterNode redisClusterNode = new RedisClusterNode(Objects.requireNonNull(clusterNode.getHost()),
                            Objects.requireNonNull(clusterNode.getPort()));
                    try (Cursor<byte[]> cursor = clusterConnection.scan(redisClusterNode, scanOptions)) {
                        while (cursor.hasNext()) {
                            byte[] resultBytes = cursor.next();
                            String base64 = "";
                            if (resultBytes[0] <= 0) {
                                // 可能是JDK序列化方式的key
                                base64 = Base64.getEncoder().encodeToString(resultBytes);
                            }
                            list.add(Pair.of(new String(resultBytes, StandardCharsets.UTF_8), base64));
                            if (list.size() >= 100) {
                                break outer;
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return list;
        });
        if (Objects.requireNonNull(resList).size() > 200) {
            // 限制返回的key数量,防止过多数据导致浏览器卡住
            resList = resList.subList(0, 200);
        }
        return resList.stream()
                .distinct()
                .sorted(Comparator.comparing(Pair::getLeft))
                .map(pair -> Tree.builder()
                        .label(pair.getLeft())
                        .labelBase64(pair.getRight())
                        .isLeaf(true)
                        .build())
                .collect(Collectors.toList());
    }


    @Override
    public ValueVo getValue(CommonVo commonVo) {
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.getRedisTemplate(commonVo.getConnectId(),
                commonVo.getRedisDbIndex());
        byte[] keyBytes;
        if (StringUtils.isNotBlank(commonVo.getRedisKeyBase64())) {
            keyBytes = Base64.getDecoder().decode(commonVo.getRedisKeyBase64());
        } else {
            keyBytes = commonVo.getRedisKey().getBytes(StandardCharsets.UTF_8);
        }
        Object res = redisTemplate.execute((RedisCallback<Object>) connection -> {
            DataType type = connection.keyCommands().type(keyBytes);
            if (type == DataType.NONE) {
                throw new RuntimeException(commonVo.getRedisKey() + " key不存在");
            }
            Long ttl = connection.keyCommands().ttl(keyBytes);
            ValueVo valueVo = ValueVo.builder()
                    .redisKeyTtl(ttl + "")
                    .redisKeyType(Objects.requireNonNull(type).code())
                    .build();
            List<FieldVo> fieldVos = Collections.emptyList();
            if (type == DataType.STRING) {
                byte[] bytes = connection.stringCommands().get(keyBytes);
                String value = bytes != null ? new String(bytes, StandardCharsets.UTF_8) : "";
                valueVo.setRedisValue(value);
                valueVo.setRedisValueSize(bytes != null ? bytes.length : null);
            } else if (type == DataType.HASH) {
                Long fieldCount = connection.hashCommands().hLen(keyBytes);
                if (fieldCount != null) {
                    valueVo.setFieldCount(fieldCount.intValue());
                    List<byte[]> bytesKeys = Lists.newArrayList(Objects.requireNonNull(connection.hashCommands().hKeys(keyBytes)));
                    bytesKeys = bytesKeys.size() <= 500 ? bytesKeys : bytesKeys.subList(0, 500);
                    List<byte[]> bytesValues = connection.hashCommands().hMGet(keyBytes, bytesKeys.toArray(new byte[0][]));
                    Objects.requireNonNull(bytesValues);
                    List<byte[]> finalBytesKeys = bytesKeys;
                    fieldVos = IntStream.range(0, bytesKeys.size())
                            .mapToObj(i -> FieldVo.builder()
                                    .fieldKey(new String(finalBytesKeys.get(i), StandardCharsets.UTF_8))
                                    .fieldValue(new String(bytesValues.get(i), StandardCharsets.UTF_8))
                                    .fieldValueSize(bytesValues.get(i).length)
                                    .build())
                            .collect(Collectors.toList());
                }
            } else if (type == DataType.LIST) {
                Long len = connection.listCommands().lLen(keyBytes);
                if (len != null) {
                    valueVo.setFieldCount(len.intValue());
                    len = len <= 500 ? len : 500;
                    List<byte[]> bytesValues = connection.listCommands().lRange(keyBytes, 0, len);
                    fieldVos = Objects.requireNonNull(bytesValues).stream()
                            .map(bytes -> FieldVo.builder()
                                    .fieldValue(new String(bytes, StandardCharsets.UTF_8))
                                    .fieldValueSize(bytes.length)
                                    .build())
                            .collect(Collectors.toList());
                    valueVo.setFieldVos(fieldVos);
                }
            } else if (type == DataType.SET) {
                Long len = connection.setCommands().sCard(keyBytes);
                if (len != null) {
                    valueVo.setFieldCount(len.intValue());
                    List<byte[]> bytesValues = connection.setCommands().sRandMember(keyBytes, 500);
                    fieldVos = Objects.requireNonNull(bytesValues).stream()
                            .map(bytes -> FieldVo.builder()
                                    .fieldValue(new String(bytes, StandardCharsets.UTF_8))
                                    .fieldValueSize(bytes.length)
                                    .build())
                            .collect(Collectors.toList());
                }
            } else if (type == DataType.ZSET) {
                Long len = connection.zSetCommands().zCard(keyBytes);
                if (len != null) {
                    valueVo.setFieldCount(len.intValue());
                    Set<Tuple> tuples = connection.zSetCommands().zRangeWithScores(keyBytes, 0, 500);
                    fieldVos = Objects.requireNonNull(tuples).stream()
                            .map(tuple -> FieldVo.builder()
                                    .fieldScore(tuple.getScore())
                                    .fieldValue(new String(tuple.getValue(), StandardCharsets.UTF_8))
                                    .fieldValueSize(tuple.getValue().length)
                                    .build())
                            .collect(Collectors.toList());
                }
            }
            valueVo.setFieldVos(fieldVos);
            return valueVo;
        });
        return (ValueVo) res;
    }

    @Override
    public String delField(CommonVo commonVo) {
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.getRedisTemplate(commonVo.getConnectId(),
                commonVo.getRedisDbIndex());
        byte[] keyBytes = commonVo.getRedisKey().getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8);
        DataType dataType = DataType.fromCode(commonVo.getRedisKeyType());
        Object res = redisTemplate.execute((RedisCallback<Object>) connection -> {
            if (Boolean.FALSE.equals(connection.keyCommands().exists(keyBytes))) {
                throw new RuntimeException(commonVo.getRedisKey() + " key不存在");
            }
            if (dataType == DataType.HASH) {
                connection.hashCommands().hDel(keyBytes, commonVo.getFieldKey().getBytes(StandardCharsets.UTF_8));
            } else if (dataType == DataType.LIST) {
                connection.listCommands().lRem(keyBytes, 1, valueBytes);
            } else if (dataType == DataType.SET) {
                connection.setCommands().sRem(keyBytes, valueBytes);
            } else if (dataType == DataType.ZSET) {
                connection.zSetCommands().zRem(keyBytes, valueBytes);
            }
            return "删除成功";
        });
        return (String) res;
    }

    @Override
    public String addField(CommonVo commonVo) {
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.getRedisTemplate(commonVo.getConnectId(),
                commonVo.getRedisDbIndex());
        byte[] keyBytes = commonVo.getRedisKey().getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8);
        byte[] fieldKeyBytes = commonVo.getFieldKey().getBytes(StandardCharsets.UTF_8);
        DataType dataType = DataType.fromCode(commonVo.getRedisKeyType());
        Object res = redisTemplate.execute((RedisCallback<Object>) connection -> {
            if (Boolean.FALSE.equals(connection.keyCommands().exists(keyBytes))) {
                throw new RuntimeException(commonVo.getRedisKey() + " key不存在");
            }
            if (dataType == DataType.HASH) {
                connection.hashCommands().hSet(keyBytes, fieldKeyBytes, valueBytes);
            } else if (dataType == DataType.LIST) {
                connection.listCommands().lPush(keyBytes, valueBytes);
            } else if (dataType == DataType.SET) {
                connection.setCommands().sAdd(keyBytes, valueBytes);
            } else if (dataType == DataType.ZSET) {
                connection.zSetCommands().zAdd(keyBytes, commonVo.getScore(), valueBytes);
            }
            return "新增成功";
        });
        return (String) res;
    }

    @Override
    public ValueVo saveValue(CommonVo commonVo) {
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.getRedisTemplate(commonVo.getConnectId(),
                commonVo.getRedisDbIndex());
        byte[] keyBytes = commonVo.getRedisKey().getBytes(StandardCharsets.UTF_8);
        Object res = redisTemplate.execute((RedisCallback<Object>) connection -> {
            DataType type = connection.keyCommands().type(keyBytes);
            if (type == DataType.NONE) {
                throw new RuntimeException(commonVo.getRedisKey() + " key不存在");
            }
            if (StringUtils.isNotBlank(commonVo.getRedisKeyTtl()) && !commonVo.getRedisKeyTtl().equals("-1")) {
                connection.keyCommands().expire(keyBytes, Integer.parseInt(commonVo.getRedisKeyTtl()));
            }
            if (type == DataType.STRING) {
                connection.stringCommands().set(keyBytes, commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8));
            } else if (type == DataType.HASH) {
                byte[] valueBytes = commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8);
                byte[] fieldKeyBytes = commonVo.getFieldKey().getBytes(StandardCharsets.UTF_8);
                connection.hashCommands().hSet(keyBytes, fieldKeyBytes, valueBytes);
            } else if (type == DataType.SET) {
                byte[] valueBytes = commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8);
                byte[] oldValueBytes = commonVo.getOldRedisValue().getBytes(StandardCharsets.UTF_8);
                connection.setCommands().sRem(keyBytes, oldValueBytes);
                connection.setCommands().sAdd(keyBytes, valueBytes);
            }
            return ValueVo.builder()
                    .redisValue(commonVo.getRedisValue())
                    .redisValueSize(commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8).length)
                    .redisKeyTtl(commonVo.getRedisKeyTtl() + "")
                    .build();
        });
        return (ValueVo) res;
    }

    @Override
    public String setNewTtl(CommonVo commonVo) {
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.getRedisTemplate(commonVo.getConnectId(),
                commonVo.getRedisDbIndex());
        byte[] keyBytes = commonVo.getRedisKey().getBytes(StandardCharsets.UTF_8);
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            DataType type = connection.keyCommands().type(keyBytes);
            if (type == DataType.NONE) {
                throw new RuntimeException(commonVo.getRedisKey() + " key不存在");
            }
            connection.keyCommands().expire(keyBytes, Integer.parseInt(commonVo.getRedisKeyTtl()));
            return null;
        });
        return "设置成功";
    }

    @Override
    public ValueVo addKey(CommonVo commonVo) {
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.getRedisTemplate(commonVo.getConnectId(),
                commonVo.getRedisDbIndex());
        byte[] keyBytes = commonVo.getRedisKey().getBytes(StandardCharsets.UTF_8);
        DataType dataType = DataType.fromCode(commonVo.getRedisKeyType());
        Object res = redisTemplate.execute((RedisCallback<Object>) connection -> {
            if (Boolean.TRUE.equals(connection.keyCommands().exists(keyBytes))) {
                throw new RuntimeException(commonVo.getRedisKey() + " key已存在");
            }
            if (dataType == DataType.STRING) {
                connection.stringCommands().set(keyBytes, commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8));
            } else if (dataType == DataType.LIST) {
                connection.listCommands().lPush(keyBytes, commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8));
            } else if (dataType == DataType.SET) {
                connection.setCommands().sAdd(keyBytes, commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8));
            } else if (dataType == DataType.HASH) {
                if (commonVo.getRedisValue() != null) {
                    connection.hashCommands().hSet(keyBytes, commonVo.getFieldKey().getBytes(StandardCharsets.UTF_8),
                            commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8));
                }
                if (commonVo.getFieldVos() != null) {
                    for (FieldVo fieldVo : commonVo.getFieldVos()) {
                        connection.hashCommands().hSet(keyBytes, fieldVo.getFieldKey().getBytes(StandardCharsets.UTF_8),
                                fieldVo.getFieldValue().getBytes(StandardCharsets.UTF_8));
                    }
                }
            } else if (dataType == DataType.ZSET) {
                if (commonVo.getRedisValue() != null) {
                    connection.zSetCommands().zAdd(keyBytes, commonVo.getScore(),
                            commonVo.getRedisValue().getBytes(StandardCharsets.UTF_8));
                }
                if (commonVo.getFieldVos() != null) {
                    for (FieldVo fieldVo : commonVo.getFieldVos()) {
                        connection.zSetCommands().zAdd(keyBytes, fieldVo.getFieldScore(),
                                fieldVo.getFieldValue().getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            if (StringUtils.isNotBlank(commonVo.getRedisKeyTtl()) && !commonVo.getRedisKeyTtl().equals("-1")) {
                connection.keyCommands().expire(keyBytes, Integer.parseInt(commonVo.getRedisKeyTtl()));
            }
            return commonVo;
        });
        return (ValueVo) res;
    }


    @Override
    public String delKey(CommonVo commonVo) {
        RedisTemplate<Object, Object> redisTemplate = RedisUtils.getRedisTemplate(commonVo.getConnectId(),
                commonVo.getRedisDbIndex());
        byte[] keyBytes = commonVo.getRedisKey().getBytes(StandardCharsets.UTF_8);
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            DataType type = connection.keyCommands().type(keyBytes);
            if (type == DataType.NONE) {
                throw new RuntimeException(commonVo.getRedisKey() + " key不存在");
            }
            connection.keyCommands().del(keyBytes);
            return null;
        });
        return "删除成功";
    }

    @Override
    public ValueVo renameKey(CommonVo commonVo) {
        String key = commonVo.getRedisKey();
        commonVo.setRedisKey(commonVo.getOldRedisKey());
        ValueVo value = getValue(commonVo);
        commonVo.setFieldVos(value.getFieldVos());
        delKey(commonVo);
        commonVo.setRedisKey(key);
        addKey(commonVo);
        return value;
    }

    @Override
    public String pingConnect(ConnectionVo connectionVoReq) {
        if (connectionVoReq.getHost().contains(",")) {
            connectionVoReq.setNodes(connectionVoReq.getHost());
            connectionVoReq.setHost("");
        }
        JedisConnectionFactory factory = RedisUtils.getJedisConnectionFactory(connectionVoReq, DEFAULT_DATABASE);
        RedisConnection connection = factory.getConnection();
        connection.close();
        return connectionVoReq.getHost() + "连接成功";
    }

}
