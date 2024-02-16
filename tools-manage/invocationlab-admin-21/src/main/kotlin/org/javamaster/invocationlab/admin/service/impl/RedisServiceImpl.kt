package org.javamaster.invocationlab.admin.service.impl

import org.javamaster.invocationlab.admin.config.BizException
import org.javamaster.invocationlab.admin.model.erd.TokenVo
import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo
import org.javamaster.invocationlab.admin.model.redis.ConnectionVo
import org.javamaster.invocationlab.admin.model.redis.Tree
import org.javamaster.invocationlab.admin.model.redis.ValueVo
import org.javamaster.invocationlab.admin.redis.RedisHelper
import org.javamaster.invocationlab.admin.redis.RedisStrategy
import org.javamaster.invocationlab.admin.redis.RedisStrategy.Companion.getInstance
import org.javamaster.invocationlab.admin.redis.TripleFunction
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.RedisService
import org.javamaster.invocationlab.admin.util.RedisUtils.getConnectionVo
import org.javamaster.invocationlab.admin.util.RedisUtils.getJedisConnectionFactory
import org.javamaster.invocationlab.admin.util.RedisUtils.getRedisTemplate
import org.javamaster.invocationlab.admin.util.SpringUtils
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.tuple.Triple
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.DataType
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.jedis.JedisClusterConnection
import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.RedisConnectionUtils
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisCluster
import redis.clients.jedis.Protocol
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * @author yudong
 */

@Service
class RedisServiceImpl : RedisService {
    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var redisHelper: RedisHelper

    @Autowired
    private val redisTemplateJackson: RedisTemplate<String, Any>? = null

    override fun pingConnect(connectionVoReq: ConnectionVo): String {
        if (connectionVoReq.host!!.contains(",")) {
            connectionVoReq.nodes = connectionVoReq.host
            connectionVoReq.host = ""
        }
        val factory = getJedisConnectionFactory(connectionVoReq, Protocol.DEFAULT_DATABASE)
        factory.connection.use { }
        return connectionVoReq.host + "连接成功"
    }

    override fun saveConnect(connectionVoReq: ConnectionVo): String {
        if (connectionVoReq.host!!.contains(",")) {
            connectionVoReq.nodes = connectionVoReq.host
            connectionVoReq.host = ""
        }
        val connectId = RandomStringUtils.randomAlphabetic(12)
        connectionVoReq.connectId = connectId
        connectionVoReq.createTime = System.currentTimeMillis()
        redisTemplateJackson!!.opsForHash<Any, Any>().put(HASH_KEY_DBS, connectId, connectionVoReq)
        return "保存成功"
    }

    override fun listConnects(): List<ConnectionVo> {
        val values = redisTemplateJackson!!.opsForHash<Any, Any>().values(HASH_KEY_DBS)
        return values.stream()
            .map { obj: Any ->
                val connectionVo = obj as ConnectionVo
                connectionVo.password = ""
                connectionVo.user = ""
                if (connectionVo.createTime == null) {
                    connectionVo.createTime = System.currentTimeMillis()
                }
                connectionVo
            }
            .sorted { a, b ->
                a.createTime!!.compareTo(b.createTime!!)
            }
            .collect(Collectors.toList())
    }

    override fun listDb(connectId: String): List<Tree> {
        val redisTemplate = getRedisTemplate(connectId, Protocol.DEFAULT_DATABASE)
        return redisTemplate.execute { connection: RedisConnection ->
            val redisConnection = connection.nativeConnection
            val pair: Pair<Int, Map<Int, Long>>
            if (redisConnection is Jedis) {
                pair = redisHelper.getSingleDbs(redisConnection)
            } else {
                val jedisCluster = redisConnection as JedisCluster
                pair = redisHelper.getClusterDbs(jedisCluster, redisTemplate)
            }
            val dbCount = pair.left
            val map = pair.right
            IntStream.range(0, dbCount)
                .mapToObj { index: Int ->
                    Tree().apply {
                        redisDbIndex = index
                        keyCount = -1L
                        isLeaf = false
                        label = "DB" + index + "(" + map[index] + ")"
                    }
                }
                .filter { obj: Any? -> Objects.nonNull(obj) }
                .collect(Collectors.toList()) as List<Tree>
        }!!
    }

    override fun listKeys(connectId: String, redisDbIndex: Int, pattern: String): List<Tree> {
        var count = 10000
        if (pattern == "*") {
            count = 100
        }
        val redisTemplate = getRedisTemplate(connectId, redisDbIndex)
        val finalCount = count
        var resList = redisTemplate.execute(
            RedisCallback { connection: RedisConnection ->
                var list: List<Triple<String, String, Class<*>>> = arrayListOf()
                if (!pattern.contains("*") && !pattern.contains("")) {
                    val exists = connection.keyCommands().exists(pattern.toByteArray(StandardCharsets.UTF_8))
                    if (exists == true) {
                        list.addLast(Triple.of(pattern, "", null))
                    }
                    return@RedisCallback list
                }

                val nativeConnection = connection.nativeConnection
                if (nativeConnection is Jedis) {
                    list = redisHelper.getSingleDbKeys(nativeConnection, finalCount, pattern)
                } else {
                    val clusterConnection = RedisConnectionUtils
                        .getConnection(redisTemplate.connectionFactory!!) as JedisClusterConnection
                    val connectionVo = getConnectionVo(connectId)
                    list = redisHelper.getClusterDbKeys(clusterConnection, connectionVo, finalCount, pattern)
                }
                list
            })!!

        if (Objects.requireNonNull(resList).size > 200) {
            resList = resList.subList(0, 200)
        }

        return resList.stream()
            .sorted(
                Comparator.comparing { obj: Triple<String, String, Class<*>> -> obj.left }
            )
            .map { triple: Triple<String, String, Class<*>> ->
                Tree().apply {
                    label = triple.left
                    labelBase64 = triple.middle
                    isLeaf = true
                }
            }
            .collect(Collectors.toList())
    }


    override fun getValue(commonRedisVo: CommonRedisVo): ValueVo {
        return executeCommand(commonRedisVo) { connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>, redisStrategy: RedisStrategy ->
            redisStrategy.getValue(
                connection, keyPair
            )
        }
    }


    override fun saveValue(commonRedisVo: CommonRedisVo): ValueVo {
        val tokenVo = tokenVo
        log.info("{}-{} save value:{}", tokenVo.userId, tokenVo.username, commonRedisVo)

        return executeCommand(commonRedisVo) { connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>, redisStrategy: RedisStrategy ->
            redisStrategy.saveValue(
                connection, keyPair, commonRedisVo
            )
            commonRedisVo
        }
    }


    override fun delField(commonRedisVo: CommonRedisVo): String {
        val tokenVo = tokenVo
        log.info("{}-{} del field:{}", tokenVo.userId, tokenVo.username, commonRedisVo)

        return executeCommand(commonRedisVo) { connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>, redisStrategy: RedisStrategy ->
            val affect = redisStrategy.delField(
                connection, keyPair, commonRedisVo
            )
            "删除成功:$affect"
        }
    }


    override fun addField(commonRedisVo: CommonRedisVo): String {
        log.info("{}-{} add field:{}", tokenVo.userId, tokenVo.username, commonRedisVo)

        return executeCommand(commonRedisVo) { connection, keyPair, redisStrategy ->
            val affect = redisStrategy.addField(connection, keyPair, commonRedisVo)
            "新增成功:$affect"
        }
    }

    override fun setNewTtl(commonRedisVo: CommonRedisVo): String {
        val tokenVo = tokenVo
        log.info("{}-{} set new ttl:{}", tokenVo.userId, tokenVo.username, commonRedisVo)

        return executeCommand(commonRedisVo) { connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>, redisStrategy: RedisStrategy ->
            val res = redisStrategy.setTtlIfNecessary(
                connection, keyPair, commonRedisVo.redisKeyTtl!!
            )
            "设置结果:$res"
        }
    }

    override fun addKey(commonRedisVo: CommonRedisVo): ValueVo {
        val tokenVo = tokenVo
        log.info("{}-{} add key:{}", tokenVo.userId, tokenVo.username, commonRedisVo)

        val redisTemplate = getRedisTemplate(commonRedisVo.connectId!!, commonRedisVo.redisDbIndex!!)

        val keyPair = redisHelper.convertKeyToBytes(
            commonRedisVo.redisKey!!, commonRedisVo.redisKeyJdkSerialize!!
        )
        val keyBytes = keyPair.left

        return redisTemplate.execute<ValueVo> { connection: RedisConnection ->
            if (true == connection.keyCommands().exists(keyBytes)) {
                throw BizException("Redis key: " + commonRedisVo.redisKey + " 已存在!!!")
            }
            val dataType = DataType.fromCode(commonRedisVo.redisKeyType!!)
            val redisStrategy = getInstance(dataType)
            val valueVo = redisStrategy.addKey(connection, keyPair, commonRedisVo)

            redisStrategy.setTtlIfNecessary(connection, keyPair, commonRedisVo.redisKeyTtl!!)
            valueVo
        }!!
    }

    override fun delKey(commonRedisVo: CommonRedisVo): String {
        val tokenVo = tokenVo
        log.info("{}-{} del key:{}", tokenVo.userId, tokenVo.username, commonRedisVo)

        return executeCommand(commonRedisVo) { connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>, redisStrategy: RedisStrategy ->
            "删除结果:" + redisStrategy.delKey(
                connection, keyPair
            )
        }
    }

    override fun renameKey(commonRedisVo: CommonRedisVo): ValueVo {
        val tokenVo = tokenVo
        log.info("{}-{} rename key:{}", tokenVo.userId, tokenVo.username, commonRedisVo)

        val redisTemplate = getRedisTemplate(commonRedisVo.connectId!!, commonRedisVo.redisDbIndex!!)

        val keyPair = redisHelper.convertKeyToBytes(
            commonRedisVo.redisKey!!, commonRedisVo.redisKeyJdkSerialize!!
        )
        val oldKeyPair = redisHelper.convertKeyToBytes(
            commonRedisVo.oldRedisKey!!,
            commonRedisVo.redisKeyBase64!!
        )

        return redisTemplate.execute { connection: RedisConnection ->
            val dataType = connection.keyCommands().type(oldKeyPair.left)
            if (dataType == DataType.NONE) {
                throw BizException("Redis key: " + commonRedisVo.oldRedisKey + " 已不存在!!!")
            }

            val redisStrategy = getInstance(dataType!!)
            redisStrategy.renameKey(connection, oldKeyPair, keyPair, commonRedisVo)
        }!!
    }

    companion object {
        const val HASH_KEY_DBS: String = "erd:redis:dbs"
        fun <U> executeCommand(
            commonRedisVo: CommonRedisVo,
            function: TripleFunction<RedisConnection, Pair<ByteArray, Class<*>>, RedisStrategy, U>
        ): U {
            val redisTemplate = getRedisTemplate(commonRedisVo.connectId!!, commonRedisVo.redisDbIndex!!)

            val redisHelper = SpringUtils.context.getBean(RedisHelper::class.java)
            val keyPair: Pair<ByteArray, Class<*>> = redisHelper.convertKeyToBytes(
                commonRedisVo.redisKey!!, commonRedisVo.redisKeyBase64!!
            )
            val keyBytes = keyPair.left

            return redisTemplate.execute { connection: RedisConnection ->
                val dataType = connection.keyCommands().type(
                    keyBytes
                )
                if (dataType == DataType.NONE) {
                    throw BizException("Redis key: " + commonRedisVo.redisKey + " 已不存在!!!")
                }

                val redisStrategy = getInstance(dataType!!)
                function.apply(connection, keyPair, redisStrategy)
            }!!
        }

        @JvmStatic
        val tokenVo: TokenVo
            get() {
                val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
                val session = requestAttributes.request.session
                var tokenVo = session.getAttribute("tokenVo") as TokenVo?
                if (tokenVo == null) {
                    tokenVo = TokenVo()
                    tokenVo.userId = "anonymous"
                    tokenVo.username = "匿名"
                }
                return tokenVo
            }
    }
}
