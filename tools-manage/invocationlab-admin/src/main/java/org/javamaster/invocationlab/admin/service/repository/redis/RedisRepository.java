package org.javamaster.invocationlab.admin.service.repository.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 封装redis相关的操作
 *
 * @author yudong
 */
@Repository
public class RedisRepository implements org.javamaster.invocationlab.admin.service.repository.Repository {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setAdd(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public Set<Object> members(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public void setRemove(String key, Object value) {
        redisTemplate.opsForSet().remove(key, value);
    }


    public void mapPut(String key, Object hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
        redisTemplate.persist(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T mapGet(String key, Object hashKey) {
        return (T) redisTemplate.opsForHash().get(key, hashKey);
    }

    public Set<Object> mapGetKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    public List<Object> mapGetValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    public void removeMap(String key, String hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }
}
