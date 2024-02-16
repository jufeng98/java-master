package org.javamaster.invocationlab.admin.service.repository.redis

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

/**
 * 封装redis相关的操作
 *
 * @author yudong
 */
@Suppress("UNCHECKED_CAST")
@Repository
class RedisRepository : org.javamaster.invocationlab.admin.service.repository.Repository {
    @Autowired
    private lateinit var redisTemplate: RedisTemplate<Any, Any>

    fun setAdd(key: String, value: Any) {
        redisTemplate.opsForSet().add(key, value)
    }

    fun <T> members(key: String): Set<T>? {
        return redisTemplate.opsForSet().members(key) as Set<T>
    }

    fun setRemove(key: String, value: Any?) {
        redisTemplate.opsForSet().remove(key, value)
    }


    fun mapPut(key: String, hashKey: Any, value: Any) {
        redisTemplate.opsForHash<Any, Any>().put(key, hashKey, value)
        redisTemplate.persist(key)
    }

    fun <T> mapGet(key: String, hashKey: Any): T? {
        return redisTemplate.opsForHash<Any, Any>()[key, hashKey] as T
    }

    fun <T> mapGetKeys(key: String): Set<T> {
        return redisTemplate.opsForHash<Any, Any>().keys(key) as Set<T>
    }

    fun delete(key: String): Boolean {
        return redisTemplate.delete(key)
    }

    fun <T> mapGetValues(key: String): List<T> {
        return redisTemplate.opsForHash<Any, Any>().values(key) as List<T>
    }

    fun removeMap(key: String, hashKey: String?) {
        redisTemplate.opsForHash<Any, Any>().delete(key, hashKey)
    }
}
