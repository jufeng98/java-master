package org.javamaster.invocationlab.admin.service.impl

import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX
import org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_QUERY_TREE
import org.javamaster.invocationlab.admin.consts.ErdConst.QUERY_TREE_TREE_NODE
import org.javamaster.invocationlab.admin.enums.CipherTypeEnum.Companion.getByType
import org.javamaster.invocationlab.admin.model.erd.*
import org.javamaster.invocationlab.admin.service.DbService
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService
import org.javamaster.invocationlab.admin.service.ErdOnlineQueryService
import org.javamaster.invocationlab.admin.util.AesUtils.decrypt
import org.javamaster.invocationlab.admin.util.AesUtils.encrypt
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Triple
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.util.function.Consumer

/**
 * @author yudong
 */
@Service

class ErdOnlineQueryServiceImpl : ErdOnlineQueryService {
    @Autowired
    private lateinit var redisTemplateJackson: RedisTemplate<String, Any>

    @Autowired
    private lateinit var erdProjectService: ErdOnlineProjectService

    @Autowired
    private lateinit var stringRedisTemplate: StringRedisTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    override fun load(projectId: String): JSONArray {
        val jsonStr = stringRedisTemplate.opsForHash<Any, Any>()[ERD_PREFIX + "load", projectId] as String
        return JSONObject.parseArray(jsonStr)
    }


    override fun getQueryTreeList(projectId: String): MutableList<Tree> {
        val jsonDataStr = stringRedisTemplate.opsForHash<Any, Any>()[PROJECT_QUERY_TREE, projectId] as String
        return objectMapper.readValue(jsonDataStr, object : TypeReference<MutableList<Tree>>() {})
    }


    override fun createDirOrQuery(reqVo: QueryReqVo, tokenVo: TokenVo): Boolean {
        val projectId = reqVo.projectId
        val trees = getQueryTreeList(
            projectId!!
        )
        val tree = Tree()
        tree.id = RandomUtils.nextLong(10000000000L, 99999999999L).toString() + ""
        tree.key = tree.id
        tree.value = tree.id
        val title = reqVo.title
        tree.title = title
        tree.label = title
        tree.isLeaf = reqVo.isLeaf
        tree.children = mutableListOf()
        val parentId = reqVo.parentId
        if (StringUtils.isNotBlank(parentId)) {
            val parent = trees.stream().filter { tree1: Tree -> tree1.id == parentId }.findAny().get()
            tree.parentId = tree.id
            tree.parentKey = tree.key
            tree.parentKeys = Lists.newArrayList<String>(null, null)
            parent.children!!.addLast(tree)
        } else {
            tree.parentKeys = mutableListOf()
            trees.add(tree)
        }
        stringRedisTemplate.opsForHash<Any, Any>()
            .put(PROJECT_QUERY_TREE, projectId, objectMapper.writeValueAsString(trees))
        stringRedisTemplate.opsForHash<Any, Any>()
            .put(QUERY_TREE_TREE_NODE, tree.id!!, objectMapper.writeValueAsString(tree))
        return true
    }


    override fun queryTree(treeNodeId: String): Tree {
        val jsonStrData = stringRedisTemplate.opsForHash<Any, Any>()[QUERY_TREE_TREE_NODE, treeNodeId] as String
        return objectMapper.readValue(jsonStrData, Tree::class.java)
    }


    override fun deleteQueryTree(projectId: String, treeNodeId: String): Int {
        val jsonStrData = stringRedisTemplate.opsForHash<Any, Any>()[PROJECT_QUERY_TREE, projectId] as String
        val trees: MutableList<Tree> =
            objectMapper.readValue(jsonStrData, object : TypeReference<MutableList<Tree>>() {})
        val del = trees.removeIf { tree: Tree -> tree.id == treeNodeId }
        if (!del) {
            trees.forEach(Consumer { tree: Tree ->
                tree.children!!.removeIf { children: Tree -> children.id == treeNodeId }
            })
        }
        stringRedisTemplate.opsForHash<Any, Any>()
            .put(PROJECT_QUERY_TREE, projectId, objectMapper.writeValueAsString(trees))
        return stringRedisTemplate.opsForHash<Any, Any>().delete(QUERY_TREE_TREE_NODE, treeNodeId).toInt()
    }


    override fun getDbs(projectId: String, tokenVo: TokenVo): List<String> {
        val dbsBean = erdProjectService.getDefaultDb(projectId, tokenVo)
        val dbService = DbService.getInstance(dbsBean.select!!)
        return dbService.getDbNames(dbsBean)
    }


    override fun getTables(reqVo: CommonErdVo, tokenVo: TokenVo): List<Table> {
        val projectId = reqVo.projectId
        val selectDB = reqVo.selectDB

        val dbsBean = erdProjectService.getDefaultDb(projectId!!, tokenVo)
        val dbService = DbService.getInstance(dbsBean.select!!)

        return dbService.getTables(dbsBean, selectDB!!)
    }


    override fun getTableColumns(reqVo: CommonErdVo, tokenVo: TokenVo): List<Column> {
        val projectId = reqVo.projectId
        val selectDB = reqVo.selectDB
        val tableName = reqVo.tableName

        val dbsBean = erdProjectService.getDefaultDb(projectId!!, tokenVo)
        val dbService = DbService.getInstance(dbsBean.select!!)

        return dbService.getTableColumns(dbsBean, selectDB!!, tableName!!)
    }

    override fun queryHistory(reqVo: CommonErdVo, tokenVo: TokenVo): JSONObject {
        val key: String = ERD_PREFIX + "sqlHistory:" + reqVo.queryId
        val size = redisTemplateJackson.opsForList().size(key)
        val start = (reqVo.page!! - 1) * reqVo.pageSize!!
        val end = start + reqVo.pageSize!! - 1
        val range = redisTemplateJackson.opsForList().range(key, start.toLong(), end.toLong())
        val tableData = JSONObject()
        tableData["records"] = range
        tableData["total"] = size
        return tableData
    }


    override fun saveQueryTree(reqVo: SaveQueryReqVo, tokenVo: TokenVo): Tree {
        val treeNodeId = reqVo.treeNodeId
        val jsonStrData = stringRedisTemplate.opsForHash<Any, Any>()[QUERY_TREE_TREE_NODE, treeNodeId!!] as String
        val tree = objectMapper.readValue(jsonStrData, Tree::class.java)

        val projectId = reqVo.projectId
        val sqlInfo = reqVo.sqlInfo
        if (StringUtils.isNotBlank(sqlInfo)) {
            tree.sqlInfo = sqlInfo
            tree.selectDB = reqVo.selectDB
        }
        val title = reqVo.title
        if (StringUtils.isNotBlank(title)) {
            tree.title = title
            tree.label = title
        }
        stringRedisTemplate.opsForHash<Any, Any>()
            .put(QUERY_TREE_TREE_NODE, treeNodeId, objectMapper.writeValueAsString(tree))

        val queryTreeList: List<Tree> = getQueryTreeList(
            projectId!!
        )
        modifyTreeList(queryTreeList, tree)

        stringRedisTemplate.opsForHash<Any, Any>()
            .put(PROJECT_QUERY_TREE, projectId, objectMapper.writeValueAsString(queryTreeList))
        return tree
    }

    private fun modifyTreeList(queryTreeList: List<Tree>, tree: Tree) {
        queryTreeList.forEach(Consumer { it: Tree ->
            if (tree.isLeaf!!) {
                it.children!!.forEach(Consumer { innerIt: Tree ->
                    if (innerIt.id == tree.id) {
                        BeanUtils.copyProperties(tree, innerIt)
                    }
                })
            } else {
                if (it.id == tree.id) {
                    it.sqlInfo = tree.sqlInfo
                    it.selectDB = tree.selectDB
                    it.label = tree.label
                    it.title = tree.title
                }
            }
        })
    }


    override fun execSql(jsonObjectReq: CommonErdVo, tokenVo: TokenVo): SqlExecResVo {
        val projectId = jsonObjectReq.projectId

        val dbsBean = erdProjectService.getDefaultDb(projectId!!, tokenVo)
        val dbService = DbService.getInstance(dbsBean.select!!)

        return dbService.execSql(jsonObjectReq, dbsBean, tokenVo)
    }


    override fun getTableRecordTotal(reqVo: CommonErdVo, tokenVo: TokenVo): Int {
        val projectId = reqVo.projectId

        val dbsBean = erdProjectService.getDefaultDb(projectId!!, tokenVo)
        val dbService = DbService.getInstance(dbsBean.select!!)

        return dbService.getTableRecordTotal(reqVo, dbsBean, tokenVo)
    }


    override fun execUpdate(jsonObjectReq: CommonErdVo, tokenVo: TokenVo): List<SqlExecResVo> {
        val projectId = jsonObjectReq.projectId

        val dbsBean = erdProjectService.getDefaultDb(projectId!!, tokenVo)
        val dbService = DbService.getInstance(dbsBean.select!!)
        return dbService.execUpdate(dbsBean, tokenVo, jsonObjectReq)
    }


    override fun exportSql(jsonObjectReq: CommonErdVo, tokenVo: TokenVo): Triple<String, MediaType, ByteArray> {
        val projectId = jsonObjectReq.projectId

        val dbsBean = erdProjectService.getDefaultDb(projectId!!, tokenVo)
        val dbService = DbService.getInstance(dbsBean.select!!)

        return dbService.exportSql(dbsBean, tokenVo, jsonObjectReq)
    }


    override fun aes(reqVo: AesReqVo, tokenVo: TokenVo): String {
        val dbsBean = erdProjectService.getDefaultDb(reqVo.projectId!!, tokenVo)

        val cipherType = dbsBean.properties!!.cipherType
        val cipherKey = dbsBean.properties!!.cipherKey
        if (StringUtils.isBlank(cipherType) || StringUtils.isBlank(cipherKey)) {
            throw ErdException("请先配置密钥信息")
        }

        val cipherTypeEnum = getByType(cipherType!!)

        val value = reqVo.value
        return if ("decrypt" == reqVo.opType) {
            decrypt(value, cipherTypeEnum, cipherKey!!)!!
        } else {
            encrypt(value, cipherTypeEnum, cipherKey!!)!!
        }
    }

    companion object {
        const val INDEX: String = "index"
        const val KEY: String = "key"
    }
}
