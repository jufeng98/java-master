package org.javamaster.invocationlab.admin.service

import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.model.erd.*
import org.javamaster.invocationlab.admin.service.impl.MySqlDbServiceImpl
import org.javamaster.invocationlab.admin.service.impl.PostgreSqlDbServiceImpl
import com.alibaba.druid.DbType
import org.apache.commons.lang3.tuple.Triple
import org.springframework.http.MediaType

interface DbService {
    fun checkDb(dbsBean: DbsBean)

    fun getCheckSql(): String

    fun getDbNames(dbsBean: DbsBean): List<String>

    fun getTables(dbsBean: DbsBean, selectDB: String): List<Table>

    fun getDbType(): DbType

    fun execUpdate(dbsBean: DbsBean, tokenVo: TokenVo, reqVo: CommonErdVo): List<SqlExecResVo>

    fun getTableColumns(dbsBean: DbsBean, selectDB: String, tableName: String): List<Column>

    fun execSql(reqVo: CommonErdVo, dbsBean: DbsBean, tokenVo: TokenVo): SqlExecResVo

    fun exportSql(dbsBean: DbsBean, tokenVo: TokenVo, reqVo: CommonErdVo): Triple<String, MediaType, ByteArray>

    fun getTableRecordTotal(reqVo: CommonErdVo, dbsBean: DbsBean, tokenVo: TokenVo): Int

    fun refreshModule(erdOnlineModel: ErdOnlineModel, moduleName: String): ModulesBean

    companion object {
        fun getInstance(select: String): DbService {
            val dbService = DB_MAP[select] ?: throw ErdException("暂时不支持当前数据库:$select")
            return dbService
        }

        private val DB_MAP: Map<String, DbService> = object : HashMap<String, DbService>() {
            init {
                put("MYSQL", MySqlDbServiceImpl())
                put("PostgreSQL", PostgreSqlDbServiceImpl())
            }
        }
    }
}
