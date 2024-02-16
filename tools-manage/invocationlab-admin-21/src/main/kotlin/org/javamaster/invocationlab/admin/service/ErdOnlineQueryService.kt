package org.javamaster.invocationlab.admin.service

import org.javamaster.invocationlab.admin.model.erd.*
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.apache.commons.lang3.tuple.Triple
import org.springframework.http.MediaType

/**
 * @author yudong
 */
interface ErdOnlineQueryService {

    fun load(projectId: String): JSONArray


    fun getQueryTreeList(projectId: String): MutableList<Tree>


    fun createDirOrQuery(reqVo: QueryReqVo, tokenVo: TokenVo): Boolean


    fun queryTree(treeNodeId: String): Tree


    fun deleteQueryTree(projectId: String, treeNodeId: String): Int

    fun queryHistory(reqVo: CommonErdVo, tokenVo: TokenVo): JSONObject


    fun saveQueryTree(reqVo: SaveQueryReqVo, tokenVo: TokenVo): Tree


    fun execSql(jsonObjectReq: CommonErdVo, tokenVo: TokenVo): SqlExecResVo


    fun execUpdate(jsonObjectReq: CommonErdVo, tokenVo: TokenVo): List<SqlExecResVo>


    fun getDbs(projectId: String, tokenVo: TokenVo): List<String>


    fun getTables(reqVo: CommonErdVo, tokenVo: TokenVo): List<Table>


    fun getTableColumns(reqVo: CommonErdVo, tokenVo: TokenVo): List<Column>


    fun exportSql(jsonObjectReq: CommonErdVo, tokenVo: TokenVo): Triple<String, MediaType, ByteArray>


    fun aes(reqVo: AesReqVo, tokenVo: TokenVo): String


    fun getTableRecordTotal(reqVo: CommonErdVo, tokenVo: TokenVo): Int
}
