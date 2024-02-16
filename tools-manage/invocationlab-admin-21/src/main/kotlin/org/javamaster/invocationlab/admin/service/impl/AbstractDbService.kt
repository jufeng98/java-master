package org.javamaster.invocationlab.admin.service.impl

import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX
import org.javamaster.invocationlab.admin.consts.ErdConst.QUERY_RES
import org.javamaster.invocationlab.admin.enums.SqlTypeEnum
import org.javamaster.invocationlab.admin.enums.SqlTypeEnum.Companion.getByType
import org.javamaster.invocationlab.admin.model.erd.*
import org.javamaster.invocationlab.admin.service.DbService
import org.javamaster.invocationlab.admin.util.DbUtils
import org.javamaster.invocationlab.admin.util.DbUtils.getDefaultDb
import org.javamaster.invocationlab.admin.util.DbUtils.jdbcTemplateSingleton
import org.javamaster.invocationlab.admin.util.DbUtils.resolveUrlDbName
import org.javamaster.invocationlab.admin.util.DbUtils.transactionTemplateSingleton
import org.javamaster.invocationlab.admin.util.ErdUtils.findModulesBean
import org.javamaster.invocationlab.admin.util.ErdUtils.tableToEntity
import org.javamaster.invocationlab.admin.util.ExecutorUtils.startAsyncTask
import org.javamaster.invocationlab.admin.util.JsonUtils.STANDARD_PATTERN
import org.javamaster.invocationlab.admin.util.JsonUtils.objectToString
import org.javamaster.invocationlab.admin.util.JsonUtils.parseObject
import org.javamaster.invocationlab.admin.util.SessionUtils.getFromSession
import org.javamaster.invocationlab.admin.util.SessionUtils.saveToSession
import org.javamaster.invocationlab.admin.util.SpringUtils
import com.alibaba.druid.sql.SQLUtils
import com.alibaba.druid.sql.ast.SQLLimit
import com.alibaba.druid.sql.ast.SQLStatement
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr
import com.alibaba.druid.sql.ast.statement.*
import com.alibaba.excel.support.ExcelTypeEnum
import com.alibaba.excel.write.builder.ExcelWriterBuilder
import com.alibaba.excel.write.metadata.WriteSheet
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.lang.time.DateFormatUtils
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Pair
import org.apache.commons.lang3.tuple.Triple
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.TypeHandler
import org.apache.ibatis.type.TypeHandlerRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import java.io.ByteArrayOutputStream
import java.lang.reflect.ParameterizedType
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors


abstract class AbstractDbService : DbService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    private val atomicInteger: AtomicInteger = AtomicInteger(0)

    override fun getCheckSql(): String {
        return "select now()"
    }

    protected open fun getExplainSql(querySql: String): String {
        return "explain $querySql"
    }

    override fun checkDb(dbsBean: DbsBean) {
        try {
            val propertiesBean = dbsBean.properties
            val urlDbName = resolveUrlDbName(propertiesBean!!.url!!)

            val jdbcTemplate = jdbcTemplateSingleton(propertiesBean, urlDbName)
            jdbcTemplate.execute(getCheckSql())
        } catch (e: Exception) {
            throw ErdException(e.message!!)
        }
    }

    override fun getDbNames(dbsBean: DbsBean): List<String> {
        val propertiesBean = dbsBean.properties
        val urlDbName = resolveUrlDbName(propertiesBean!!.url!!)

        val jdbcTemplate = jdbcTemplateSingleton(propertiesBean, urlDbName)
        return getDbNames(jdbcTemplate)
    }

    abstract fun getDbNames(jdbcTemplate: JdbcTemplate?): List<String>

    private fun tryGetTableNameIfSingleTableQuery(sql: String?): String {
        val sqlStatement = SQLUtils.parseSingleStatement(sql, getDbType()) as SQLSelectStatement
        val sqlSelectQuery = sqlStatement.select.query

        return tryGetTableNameIfSingleTableQuery(sqlSelectQuery)
    }

    /**
     * 如果是单表简单查询,则返回表名,否则返回空
     */
    private fun tryGetTableNameIfSingleTableQuery(sqlSelectQuery: SQLSelectQuery): String {
        if (sqlSelectQuery !is SQLSelectQueryBlock) {
            return ""
        }

        val sqlTableSource = sqlSelectQuery.from as? SQLExprTableSource ?: return ""

        val groupBy = sqlSelectQuery.groupBy
        if (groupBy != null) {
            return ""
        }

        val hasMethod = sqlSelectQuery.selectList.stream()
            .anyMatch { it: SQLSelectItem -> it.expr is SQLMethodInvokeExpr }
        if (hasMethod) {
            return ""
        }

        return sqlTableSource.tableName
    }

    override fun getTables(dbsBean: DbsBean, selectDB: String): List<Table> {
        val jdbcTemplate = jdbcTemplateSingleton(dbsBean.properties!!, selectDB)
        return getTables(jdbcTemplate)
    }

    private fun getTables(jdbcTemplate: JdbcTemplate): List<Table> {
        return DbUtils.getTables(jdbcTemplate)
    }


    private fun convertToSuitableObj(column: Column, `val`: String): Any {
        val typeHandler = typeHandlerRegistry.getTypeHandler(
            JdbcType.forCode(
                column.type
            )
        )
        val typeHandlerClass: Class<*> = typeHandler.javaClass

        val genericInterfaces = typeHandlerClass.genericSuperclass as ParameterizedType
        val actualType = genericInterfaces.actualTypeArguments[0] as Class<*>

        val conversionService: ConversionService = SpringUtils.context.getBean(
            ConversionService::class.java
        )
        return conversionService.convert(`val`, actualType)!!
    }

    protected open fun modifyQueryRow(rowMap: MutableMap<String, Any>) {
    }

    private fun modifyRowForSpecialColumnType(rowMap: MutableMap<String, Any>) {
        for (rowEntry in rowMap.entries) {
            var columnValue = rowEntry.value
            if (columnValue is Long
                || columnValue is BigInteger
                || columnValue is BigDecimal
            ) {
                columnValue = columnValue.toString()
            }
            rowMap[rowEntry.key] = columnValue
        }
    }

    override fun execUpdate(dbsBean: DbsBean, tokenVo: TokenVo, reqVo: CommonErdVo): List<SqlExecResVo> {
        val jdbcTemplate = jdbcTemplateSingleton(dbsBean.properties!!, reqVo.selectDB!!)
        val transactionTemplate = transactionTemplateSingleton(dbsBean.properties!!, reqVo.selectDB!!)
        return execUpdate(jdbcTemplate, transactionTemplate, tokenVo, reqVo)
    }

    private fun execUpdate(
        jdbcTemplate: JdbcTemplate, transactionTemplate: TransactionTemplate,
        tokenVo: TokenVo, reqVo: CommonErdVo
    ): List<SqlExecResVo> {
        val tableName = tryGetTableNameIfSingleTableQuery(reqVo.sql)
        val tableColumns: List<Column> = getTableColumns(jdbcTemplate, tableName)
        val columnMap = Maps.uniqueIndex(tableColumns) { it!!.name }
        val primaryKeyNames = filterPrimaryKeys(tableColumns)

        return transactionTemplate.execute<List<SqlExecResVo>> {
            reqVo.rows!!.stream()
                .map { tmpRow: Any ->
                    // 过滤掉值为null的列
                    val row = parseObject(objectToString(tmpRow), JSONObject::class.java)
                    val pairPrimary = primaryKeyConditions(row, columnMap, primaryKeyNames)

                    val tmpPair: Pair<String, List<Pair<Any, JdbcType>>>
                    val sqlTypeEnum = getByType(row.getString(ROW_OPERATION_TYPE))
                    tmpPair = when (sqlTypeEnum) {
                        SqlTypeEnum.DELETE -> {
                            handleDeleteOperation(columnMap, pairPrimary!!, row, tableName)
                        }

                        SqlTypeEnum.UPDATE -> {
                            handleEditOperation(columnMap, pairPrimary!!, row, tableName)
                        }

                        SqlTypeEnum.INSERT -> {
                            handleInsertOperation(columnMap, row, tableName)
                        }

                        else -> {
                            throw IllegalArgumentException(sqlTypeEnum.type)
                        }
                    }

                    val actualSql = tmpPair.left
                    val fieldValues = tmpPair.right

                    val index = row.getIntValue(INDEX)
                    executeUpdateSql(jdbcTemplate, actualSql, tokenVo, reqVo.queryId!!, fieldValues, index)
                }
                .collect(Collectors.toList())
        }!!
    }

    private fun getPrimaryKeys(jdbcTemplate: JdbcTemplate, tableName: String): List<String> {
        val tableColumns = getPrimaryColumns(jdbcTemplate, tableName)
        return tableColumns.stream()
            .map<String>(Column::name)
            .collect(Collectors.toList())
    }

    private fun getPrimaryColumns(jdbcTemplate: JdbcTemplate, tableName: String): List<Column> {
        val tableColumns = getTableColumns(jdbcTemplate, tableName)
        return tableColumns.stream()
            .filter(Column::primaryKey)
            .collect(Collectors.toList())
    }

    protected fun filterPrimaryKeys(tableColumns: List<Column>): List<String> {
        return tableColumns.stream()
            .filter(Column::primaryKey)
            .map<String>(Column::name)
            .collect(Collectors.toList())
    }

    private fun convertRowToRealFields(
        oldRow: Map<String, Any>,
        columnMap: ImmutableMap<String, Column>
    ): List<Pair<Any, JdbcType>> {
        return oldRow.entries.stream()
            .map { entry: Map.Entry<String, Any> ->
                val column = columnMap[entry.key]!!
                val `val` = convertToSuitableObj(column, entry.value as String)
                Pair.of(`val`, JdbcType.forCode(column.type))
            }
            .collect(Collectors.toList())
    }

    private fun handleDeleteOperation(
        columnMap: ImmutableMap<String, Column>,
        pairPrimary: Pair<String, List<Pair<Any, JdbcType>>>,
        row: JSONObject,
        tableName: String
    ): Pair<String, List<Pair<Any, JdbcType>>> {
        val sql: String
        val fieldValues: MutableList<Pair<Any, JdbcType>> = Lists.newArrayList()
        val primaryValues = pairPrimary.right

        val noPrimaryKeyInRequest =
            primaryValues.stream().anyMatch { obj: Pair<Any, JdbcType> -> Objects.isNull(obj) }
        if (noPrimaryKeyInRequest) {
            val oldRow = getOldQueryMap(row[KEY]!!)
            oldRow!!.remove(KEY)

            val conditions = oldRow.keys.stream()
                .map { key: String -> "$key=" }
                .collect(Collectors.joining(" AND "))
            sql = String.format("delete from %s where %s", tableName, conditions)

            val pairs = convertRowToRealFields(oldRow, columnMap)
            fieldValues.addAll(pairs)
        } else {
            sql = String.format("delete from %s where %s", tableName, pairPrimary.left)
            fieldValues.addAll(primaryValues)
        }
        return Pair.of(sql, fieldValues)
    }

    private fun handleEditOperation(
        columnMap: ImmutableMap<String, Column>,
        pairPrimary: Pair<String, List<Pair<Any, JdbcType>>>,
        row: JSONObject, tableName: String
    ): Pair<String, List<Pair<Any, JdbcType>>> {
        val oldRow = getOldQueryMap(row[KEY]!!)!!

        val sql: String
        val fieldValues: MutableList<Pair<Any, JdbcType>> = Lists.newArrayList()

        val fields = row.entries.stream()
            .filter { entry: Map.Entry<String, Any> -> this.filterUnRelateColumn(entry) }
            .filter { entry: Map.Entry<String, Any> -> filterUnChangeColumn(entry, oldRow) }
            .map { entry: Map.Entry<String, Any> ->
                val columnName = entry.key
                val columnValue = entry.value.toString()

                val column = columnMap[columnName]
                val jdbcType = JdbcType.forCode(column!!.type)

                if (NULL_VALUE == columnValue) {
                    fieldValues.add(Pair.of(null, jdbcType))
                } else {
                    val `val` = convertToSuitableObj(column, columnValue)
                    fieldValues.add(Pair.of(`val`, JdbcType.forCode(column.type)))
                }
                "$columnName=?"
            }
            .collect(Collectors.joining(","))
        if (StringUtils.isBlank(fields)) {
            throw ErdException("数据无变化,未做任何更新操作!")
        }

        val primaryValues = pairPrimary.right
        val noPrimaryKeyInRequest =
            primaryValues.stream().anyMatch { obj: Pair<Any, JdbcType> -> Objects.isNull(obj) }
        if (noPrimaryKeyInRequest) {
            oldRow.remove(KEY)

            val conditions = oldRow.keys.stream()
                .map { key: String -> "$key=?" }
                .collect(Collectors.joining(" AND "))
            sql = String.format("update %s set %s where %s", tableName, fields, conditions)

            val pairs = convertRowToRealFields(oldRow, columnMap)
            fieldValues.addAll(pairs)
        } else {
            sql = String.format("update %s set %s where %s", tableName, fields, pairPrimary.left)
            fieldValues.addAll(primaryValues)
        }
        return Pair.of(sql, fieldValues)
    }

    private fun handleInsertOperation(
        columnMap: ImmutableMap<String, Column>,
        row: JSONObject,
        tableName: String
    ): Pair<String, List<Pair<Any, JdbcType>>> {
        val fieldValues: MutableList<Pair<Any, JdbcType>> = mutableListOf()
        val fieldNames: MutableList<String> = mutableListOf()

        row.entries.stream()
            .filter { entry: Map.Entry<String, Any> -> this.filterUnRelateColumn(entry) }
            .forEach { entry: Map.Entry<String, Any> ->
                val columnName = entry.key
                val columnValue = entry.value.toString()

                val column = columnMap[columnName]
                val jdbcType = JdbcType.forCode(column!!.type)

                fieldNames.add(columnName)
                if (NULL_VALUE == columnValue) {
                    fieldValues.add(Pair.of(null, jdbcType))
                } else {
                    val objVal = convertToSuitableObj(column, columnValue)
                    fieldValues.add(Pair.of(objVal, jdbcType))
                }
            }
        val fields = java.lang.String.join(",", fieldNames)

        val values = fieldNames.stream()
            .map { "?" }
            .collect(Collectors.joining(","))

        val sql = String.format("insert into %s (%s) values (%s)", tableName, fields, values)
        return Pair.of(sql, fieldValues)
    }

    private fun executeUpdateSql(
        jdbcTemplate: JdbcTemplate, sql: String, tokenVo: TokenVo,
        queryId: String, fieldValues: List<Pair<Any, JdbcType>>, index: Int
    ): SqlExecResVo {
        log.info(
            "{}-{} execute dml sql:\n{},\nparams:{}",
            tokenVo.userId,
            tokenVo.username,
            sql,
            fieldValues
        )
        val start = System.currentTimeMillis()

        val num = jdbcTemplate.execute(sql) { ps: PreparedStatement ->
            for (i in fieldValues.indices) {
                val valuePair = fieldValues[i]
                val fieldValue = valuePair.left
                val fieldType = valuePair.right

                @Suppress("UNCHECKED_CAST")
                val typeHandler = typeHandlerRegistry.getTypeHandler(fieldType) as TypeHandler<Any>
                typeHandler.setParameter(ps, i + 1, fieldValue, fieldType)
            }
            ps.executeUpdate()
        }!!

        val end = System.currentTimeMillis()
        log.info("{}-{} execute dml sql res num:{}", tokenVo.userId, tokenVo.username, num)

        val resVo = SqlExecResVo()
        resVo.columns = Sets.newHashSet("affect_num")
        resVo.tableColumns = emptyMap()
        resVo.primaryKeys = emptyList()
        resVo.queryKey = atomicInteger.incrementAndGet()

        val tableData = TableData()
        tableData.total = 1

        val map: MutableMap<String, Any> = Maps.newHashMap()
        map["affect_num"] = num
        tableData.records = Lists.newArrayList(map)

        resVo.tableData = tableData

        if (num != 1) {
            addSqlToExecuteHistory("$sql (已回滚)", jdbcTemplate, tokenVo, queryId, fieldValues, end - start)
            throw ErdException("第 " + (index + 1) + " 行记录根据结果集条件不能确定唯一的记录,期望影响 1 条,实际会影响 " + num + " 条!")
        } else {
            addSqlToExecuteHistory(sql, jdbcTemplate, tokenVo, queryId, fieldValues, end - start)
        }
        return resVo
    }

    override fun getTableColumns(dbsBean: DbsBean, selectDB: String, tableName: String): List<Column> {
        val jdbcTemplate = jdbcTemplateSingleton(dbsBean.properties!!, selectDB)
        return getTableColumns(jdbcTemplate, tableName)
    }

    protected open fun getTableColumns(jdbcTemplate: JdbcTemplate, tableName: String): List<Column> {
        return DbUtils.getTableColumns(jdbcTemplate, tableName)
    }

    private fun isDmlSql(lowerSql: String): Boolean {
        return (lowerSql.startsWith(SqlTypeEnum.UPDATE.type)
                || lowerSql.startsWith(SqlTypeEnum.DELETE.type)
                || lowerSql.startsWith(SqlTypeEnum.INSERT.type))
    }

    private fun isDdlSql(lowerSql: String): Boolean {
        return (lowerSql.startsWith(SqlTypeEnum.CREATE.type)
                || lowerSql.startsWith(SqlTypeEnum.ALTER.type)
                || lowerSql.startsWith(SqlTypeEnum.DROP.type))
    }

    override fun execSql(reqVo: CommonErdVo, dbsBean: DbsBean, tokenVo: TokenVo): SqlExecResVo {
        val useNoneDefaultDb = dbsBean.name != reqVo.selectDB

        val jdbcTemplate = jdbcTemplateSingleton(dbsBean.properties!!, reqVo.selectDB!!)
        if (reqVo.explain!!) {
            return executeExplainSql(reqVo, jdbcTemplate, tokenVo)
        }

        val lowerSql = reqVo.sql!!.lowercase(Locale.getDefault())
        val ddlSql = isDdlSql(lowerSql)
        val dmlSql = isDmlSql(lowerSql)
        if (dmlSql || ddlSql) {
            val transactionTemplate = transactionTemplateSingleton(dbsBean.properties!!, reqVo.selectDB!!)
            return transactionTemplate.execute {
                executeDmlOrDdlSql(
                    jdbcTemplate,
                    tokenVo,
                    ddlSql,
                    reqVo
                )
            }!!
        }

        return executeDqlSql(jdbcTemplate, tokenVo, useNoneDefaultDb, reqVo)
    }

    private fun executeExplainSql(reqVo: CommonErdVo, jdbcTemplate: JdbcTemplate, tokenVo: TokenVo): SqlExecResVo {
        val explainSql = getExplainSql(reqVo.sql!!)
        log.info("{}-{} execute explain sql:\n{}", tokenVo.userId, tokenVo.username, explainSql)

        val start = System.currentTimeMillis()
        val list = jdbcTemplate.queryForList(explainSql)
        val end = System.currentTimeMillis()

        val resVo = SqlExecResVo()
        resVo.columns = list[0].keys

        val tableData = TableData()
        tableData.total = 1
        tableData.records = list
        resVo.tableData = tableData

        addSqlToExecuteHistory(explainSql, jdbcTemplate, tokenVo, reqVo.queryId!!, emptyList(), end - start)
        return resVo
    }

    private fun executeDmlOrDdlSql(
        jdbcTemplate: JdbcTemplate,
        tokenVo: TokenVo,
        ddlSql: Boolean,
        reqVo: CommonErdVo
    ): SqlExecResVo {
        val sqlList = Arrays.stream(reqVo.sql!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray())
            .collect(Collectors.toList())
        if (!ddlSql) {
            val dangerSqlList = sqlList.stream()
                .filter { tmpSql: String ->
                    val lowerSql =
                        tmpSql.lowercase(Locale.getDefault()).replace("\\s".toRegex(), " ").trim { it <= ' ' }
                    !lowerSql.startsWith(SqlTypeEnum.INSERT.type) && !lowerSql.contains("where")
                }
                .collect(Collectors.toList())
            if (dangerSqlList.isNotEmpty()) {
                throw ErdException(java.lang.String.join("、", dangerSqlList) + " 语句缺少where条件!")
            }
        }

        log.info("{}-{} execute dml sql:\n{}", tokenVo.userId, tokenVo.username, reqVo.sql)
        val start = System.currentTimeMillis()
        val num = jdbcTemplate.batchUpdate(*sqlList.toTypedArray<String>())
        val end = System.currentTimeMillis()
        log.info(
            "{}-{} execute dml sql res num:{}",
            tokenVo.userId,
            tokenVo.username,
            num.contentToString()
        )

        val resVo = SqlExecResVo()
        resVo.columns = Sets.newHashSet("affect_num")
        resVo.tableColumns = emptyMap()
        resVo.primaryKeys = emptyList()
        resVo.queryKey = atomicInteger.incrementAndGet()

        val tableData = TableData()
        tableData.total = 1

        val map: MutableMap<String, Any> = Maps.newHashMap()
        map["affect_num"] = Arrays.stream(num).sum()

        tableData.records = Lists.newArrayList(map)

        resVo.tableData = tableData

        addSqlToExecuteHistory(reqVo.sql!!, jdbcTemplate, tokenVo, reqVo.queryId!!, emptyList(), end - start)
        return resVo
    }

    private fun executeDqlSql(
        jdbcTemplate: JdbcTemplate,
        tokenVo: TokenVo,
        useNoneDefaultDb: Boolean,
        reqVo: CommonErdVo
    ): SqlExecResVo {
        var newSql = reqVo.sql
        val pair = modifyQuerySql(newSql!!, jdbcTemplate, reqVo)

        newSql = pair.left
        val showPagination = pair.right.left
        val primaryKeys = pair.right.right

        log.info("{}-{} execute dql sql:\n{}", tokenVo.userId, tokenVo.username, newSql)
        val start = System.currentTimeMillis()
        var list = jdbcTemplate.queryForList(
            newSql!!
        )
        val end = System.currentTimeMillis()
        log.info("{}-{} execute dql sql res size:{}", tokenVo.userId, tokenVo.username, list.size)

        val columns: MutableSet<String> = Sets.newLinkedHashSet()
        val tableName = pair.right.middle
        if (list.isEmpty()) {
            if (StringUtils.isNotBlank(tableName)) {
                val tableColumns = getTableColumns(jdbcTemplate, tableName!!)
                val columnNames = tableColumns.stream()
                    .map<String>(Column::name)
                    .collect(Collectors.toList())

                columns.addAll(columnNames)
                columns.add(KEY)
            }
        } else {
            columns.addAll(list[0].keys)
            columns.add(KEY)

            val anInt = (RandomUtils.nextInt(1000000, 9999999).toString() + "01").toInt()
            for (i in list.indices) {
                val rowMap = list[i]
                rowMap[KEY] = anInt + i

                modifyQueryRow(rowMap)

                if (!reqVo.isExport!!) {
                    modifyRowForSpecialColumnType(rowMap)
                }
            }
        }

        val resVo = SqlExecResVo()
        resVo.columns = columns
        resVo.queryKey = atomicInteger.incrementAndGet()
        resVo.page = reqVo.page
        resVo.pageSize = reqVo.pageSize
        resVo.showPagination = showPagination
        resVo.tableName = tableName
        resVo.primaryKeys = primaryKeys

        val tableData = TableData()
        if (list.isEmpty()) {
            tableData.realTotal = 0
        } else if (list.size > reqVo.pageSize!!) {
            list = list.subList(0, reqVo.pageSize!!)
            tableData.total = reqVo.page!! * reqVo.pageSize!! + 1
        } else {
            tableData.realTotal = (reqVo.page!! - 1) * reqVo.pageSize!! + list.size
        }
        tableData.records = list

        resVo.tableData = tableData

        if ((useNoneDefaultDb || SpringUtils.proEnv) && StringUtils.isNotBlank(tableName)) {
            val tableColumns = getTableColumns(jdbcTemplate, tableName!!)
            val columnMap = Maps.uniqueIndex(tableColumns) { it!!.name }
            resVo.tableColumns = columnMap
        } else {
            resVo.tableColumns = emptyMap()
        }

        saveToSession<List<MutableMap<String, Any>>>(QUERY_RES, list)
        addSqlToExecuteHistory(newSql, jdbcTemplate, tokenVo, reqVo.queryId!!, emptyList(), end - start)
        return resVo
    }

    private fun getOldQueryMap(rowUniqueKey: Any): MutableMap<String, Any>? {
        val oldRecords = getFromSession<List<Map<String, Any>>>(QUERY_RES)
        val list = oldRecords.stream()
            .filter { oldRecord: Map<String, Any> -> oldRecord[KEY] == rowUniqueKey }
            .collect(Collectors.toList())
        if (list.isEmpty()) {
            return null
        }
        val oldRow = list[0]
        return Maps.newLinkedHashMap(oldRow)
    }

    /**
     * 尝试根据主键信息取得主键值
     *
     * @param row               入参行
     * @param columnMap         列元数据
     * @param primaryKeyColumns 主键名列表
     * @return 返回主键信息, 格式如 Pair.of(<"id = ? and secondId = ?", [1, 2]>)
     */
    private fun primaryKeyConditions(
        row: JSONObject,
        columnMap: ImmutableMap<String, Column>,
        primaryKeyColumns: List<String>
    ): Pair<String, List<Pair<Any, JdbcType>>>? {
        val oldRow = getOldQueryMap(row[KEY]!!) ?: return null

        val primaryKeyValues = primaryKeyColumns.stream()
            .map { key: String ->
                val oldVal = oldRow[key]
                val column = columnMap[key]

                val jdbcType = JdbcType.forCode(column!!.type)
                val obj = convertToSuitableObj(column, oldVal as String)
                Pair.of(obj, jdbcType)
            }
            .collect(Collectors.toList())

        val key = primaryKeyColumns.stream()
            .map { primaryKey: String -> "$primaryKey=?" }
            .collect(Collectors.joining(" and "))

        return Pair.of(key, primaryKeyValues)
    }

    private fun filterUnRelateColumn(entry: Map.Entry<String, Any>): Boolean {
        return (ROW_OPERATION_TYPE != entry.key
                && KEY != entry.key
                && INDEX != entry.key)
    }

    private fun filterUnChangeColumn(rowEntry: Map.Entry<String, Any>, oldRowMap: Map<String, Any>): Boolean {
        val columnName = rowEntry.key
        val columnValue = rowEntry.value
        val dbColumnValue = oldRowMap[columnName]
        if (dbColumnValue is Date) {
            if (StringUtils.isEmpty(columnValue.toString())) {
                return true
            }
            return !dbColumnValue.toString().contains(columnValue.toString())
        } else if (dbColumnValue is Long
            || dbColumnValue is BigInteger
            || dbColumnValue is BigDecimal
        ) {
            if (StringUtils.isEmpty(columnValue.toString())) {
                return true
            }
            return columnValue != dbColumnValue.toString()
        }
        // 过滤掉值未发生变化的列
        return columnValue != dbColumnValue
    }

    override fun exportSql(
        dbsBean: DbsBean,
        tokenVo: TokenVo,
        reqVo: CommonErdVo
    ): Triple<String, MediaType, ByteArray> {
        val jdbcTemplate = jdbcTemplateSingleton(dbsBean.properties!!, reqVo.selectDB!!)
        return exportSql(jdbcTemplate, dbsBean, tokenVo, reqVo)
    }

    private fun exportSql(
        jdbcTemplate: JdbcTemplate, dbsBean: DbsBean,
        tokenVo: TokenVo, reqVo: CommonErdVo
    ): Triple<String, MediaType, ByteArray> {
        val oldRecords = getFromSession<List<Map<String, Any>>>(QUERY_RES)

        val resVo = execSql(reqVo, dbsBean, tokenVo)

        saveToSession(QUERY_RES, oldRecords)

        val records: List<MutableMap<String, Any>> = resVo.tableData!!.records!!
        records.forEach(Consumer { record: MutableMap<String, Any> ->
            record.remove(KEY)
            record.remove(INDEX)
        })

        val type = reqVo.type
        val sql = reqVo.sql
        var fileName = "SQL导出-" + DateFormatUtils.format(Date(), STANDARD_PATTERN) + "." + reqVo.type

        if ("json" == type) {
            val jsonStr = objectToString(records)
            return Triple.of(fileName, MediaType.TEXT_PLAIN, jsonStr.toByteArray(StandardCharsets.UTF_8))
        }

        if ("csv" == type) {
            val res = generateCsv(records)
            return Triple.of(fileName, MediaType.TEXT_PLAIN, res.toByteArray(StandardCharsets.UTF_8))
        }

        val sqlStatement = SQLUtils.parseSingleStatement(sql, getDbType()) as SQLSelectStatement
        val sqlSelectQuery = sqlStatement.select.query

        val tableName = tryGetTableNameIfSingleTableQuery(sqlSelectQuery)

        if ("xls" == type) {
            val bytes = excelBytes(records, tableName)
            return Triple.of(fileName, MediaType("application", "vnd.ms-excel"), bytes)
        }

        if (StringUtils.isBlank(tableName)) {
            throw ErdException("不支持的操作")
        }

        if ("sqlInsert" == type) {
            fileName = "INSERT SQL导出-" + DateFormatUtils.format(Date(), STANDARD_PATTERN) + ".sql"
            val tmpSql = generateInsertSql(records, sqlSelectQuery)
            return Triple.of(fileName, MediaType.TEXT_PLAIN, tmpSql.toByteArray(StandardCharsets.UTF_8))
        }

        if ("sqlUpdate" == type) {
            fileName = "UPDATE SQL导出-" + DateFormatUtils.format(Date(), STANDARD_PATTERN) + ".sql"
            val primaryColumns = getPrimaryColumns(jdbcTemplate, tableName)
            val tmpSql = generateUpdateSql(records, sqlSelectQuery, primaryColumns)
            return Triple.of(fileName, MediaType.TEXT_PLAIN, tmpSql.toByteArray(StandardCharsets.UTF_8))
        }

        throw IllegalArgumentException(type)
    }

    private fun generateCsv(records: List<MutableMap<String, Any>>): String {
        val columnNames: List<String> = ArrayList(records[0].keys)

        @Suppress("LABEL_NAME_CLASH")
        val s = records.stream()
            .map { record: Map<String, Any> ->
                record.values.stream()
                    .map { value: Any? ->
                        if (value == null) {
                            return@map "(null)"
                        }
                        when (value) {
                            is Date -> {
                                return@map String.format("\"%s\"", value)
                            }

                            is String -> {
                                return@map String.format("\"%s\"", value)
                            }

                            else -> {
                                return@map value.toString()
                            }
                        }
                    }
                    .collect(Collectors.joining("\t"))
            }
            .collect(Collectors.joining("\n"))
        return java.lang.String.join("\t", columnNames) + "\n" + s
    }

    private fun generateUpdateSql(
        records: List<MutableMap<String, Any>>, sqlSelectQuery: SQLSelectQuery,
        primaryColumns: List<Column>
    ): String {
        val tableName = tryGetTableNameIfSingleTableQuery(sqlSelectQuery)
        val columnNameList = getColumnNames(records, sqlSelectQuery)

        val primaryNames = primaryColumns.stream()
            .map<String>(Column::name)
            .collect(Collectors.toList())

        val primaryMap: MutableMap<String, Any?> = Maps.newLinkedHashMap()
        for (primaryName in primaryNames) {
            primaryMap[primaryName] = null
        }

        val columnNames: Set<String> = Sets.newHashSet(columnNameList)
        return records.stream()
            .map { record: Map<String, Any> ->
                @Suppress("LABEL_NAME_CLASH")
                val fields = record.entries.stream()
                    .map<String> { entry: Map.Entry<String, Any> ->
                        val key = entry.key
                        val value = entry.value
                        if (primaryNames.contains(key)) {
                            primaryMap[key] = value
                            return@map null
                        }
                        if (!columnNames.contains(key)) {
                            return@map null
                        }
                        entryToCondition(entry)
                    }
                    .filter { obj: String? -> Objects.nonNull(obj) }
                    .collect(Collectors.joining(", "))
                val conditions = primaryMap.entries.stream()
                    .map { entry: Map.Entry<String, Any?> -> this.entryToCondition(entry) }
                    .collect(Collectors.joining(" AND "))
                String.format("UPDATE %s SET %s WHERE %s;", tableName, fields, conditions)
            }
            .collect(Collectors.joining("\r\n"))
    }

    private fun generateInsertSql(records: List<MutableMap<String, Any>>, sqlSelectQuery: SQLSelectQuery): String {
        val tableName = tryGetTableNameIfSingleTableQuery(sqlSelectQuery)
        val columnNames = getColumnNames(records, sqlSelectQuery)

        return records.stream()
            .map { record: Map<String, Any> ->
                val fields = java.lang.String.join(", ", columnNames)

                @Suppress("LABEL_NAME_CLASH")
                val values = record.values.stream()
                    .map { value: Any? ->
                        if (value == null) {
                            return@map "null"
                        }
                        when (value) {
                            is Date -> {
                                return@map String.format("'%s'", value)
                            }

                            is String -> {
                                return@map String.format("'%s'", value)
                            }

                            else -> {
                                return@map value.toString()
                            }
                        }
                    }
                    .collect(Collectors.joining(", "))
                String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, fields, values)
            }
            .collect(Collectors.joining("\r\n"))
    }


    private fun excelBytes(records: List<MutableMap<String, Any>>, tableName: String): ByteArray {
        val list = records.stream()
            .map<ArrayList<Any>> { record: MutableMap<String, Any> ->
                modifyRowForSpecialColumnType(record)
                record.forEach { (key: String, value: Any) ->
                    if (value is Date) {
                        record.replace(key, DateFormatUtils.format(value, STANDARD_PATTERN))
                    }
                }
                Lists.newArrayList(record.values)
            }
            .collect(Collectors.toList<List<Any>>())

        ByteArrayOutputStream().use {
            val excelWriter = ExcelWriterBuilder()
                .registerWriteHandler(LongestMatchColumnWidthStyleStrategy())
                .excelType(ExcelTypeEnum.XLS)
                .autoCloseStream(true)
                .file(it)
                .build()

            val head: List<MutableList<String>> = records[0].keys.stream()
                .map<ArrayList<String>> { elements: String -> Lists.newArrayList(elements) }
                .collect(Collectors.toList())

            val sheet = WriteSheet()
            sheet.head = head
            sheet.sheetName = if (StringUtils.isNotBlank(tableName)) tableName else "记录"

            excelWriter.write(list, sheet)
            excelWriter.finish()
            return it.toByteArray()
        }
    }

    override fun refreshModule(erdOnlineModel: ErdOnlineModel, moduleName: String): ModulesBean {
        val dbsBean = getDefaultDb(erdOnlineModel)
        val modulesBean = findModulesBean(moduleName, erdOnlineModel)
        val datatypeBeans: List<DatatypeBean> = erdOnlineModel.projectJSON!!.dataTypeDomains!!.datatype!!

        val urlDbName = resolveUrlDbName(dbsBean.properties!!.url!!)
        val jdbcTemplate = jdbcTemplateSingleton(dbsBean.properties!!, urlDbName)

        return refreshModule(jdbcTemplate, modulesBean, datatypeBeans)
    }

    fun refreshModule(
        jdbcTemplate: JdbcTemplate,
        modulesBean: ModulesBean,
        datatypeBeans: List<DatatypeBean>
    ): ModulesBean {
        return jdbcTemplate.execute { con: Connection ->
            val databaseMetaData = con.metaData
            val entitiesBeans = modulesBean.entities!!.stream()
                .map { entitiesBean: EntitiesBean ->
                    val predicate = datatypePredicate()
                    val bean = tableToEntity(entitiesBean, databaseMetaData, datatypeBeans, predicate)

                    val tableName = entitiesBean.title!!
                    val ddl = getTableDdlSql(jdbcTemplate, tableName)

                    bean.originalCreateTableSql = ddl
                    bean
                }
                .collect(Collectors.toList())
            modulesBean.entities = entitiesBeans
            modulesBean
        }!!
    }

    protected abstract fun datatypePredicate(): Predicate<Pair<ApplyBean, Column>>

    protected abstract fun getTableDdlSql(jdbcTemplate: JdbcTemplate, tableName: String): String

    private fun modifyQuerySql(
        sql: String,
        jdbcTemplate: JdbcTemplate,
        reqVo: CommonErdVo
    ): Pair<String, Triple<Boolean, String?, List<String>>> {
        var newSql = sql
        val sqlStatement: SQLStatement = SQLUtils.parseSingleStatement(newSql, getDbType())
        if (sqlStatement !is SQLSelectStatement) {
            return Pair.of(
                sql,
                Triple.of(false, null, Lists.newArrayList())
            )
        }

        val sqlSelectQuery = sqlStatement.select.query

        var noLimit = false
        var tablePrimaryKeys: List<String> = Lists.newArrayList()
        var tableName: String? = null

        if (sqlSelectQuery is SQLSelectQueryBlock) {
            if (sqlSelectQuery.limit == null) {
                noLimit = true
                val offset = (reqVo.page!! - 1) * reqVo.pageSize!!
                sqlSelectQuery.limit(reqVo.pageSize!! + 1, offset)
            }

            tableName = tryGetTableNameIfSingleTableQuery(sqlSelectQuery)
            if (StringUtils.isNotBlank(tableName)) {
                tablePrimaryKeys = getPrimaryKeys(jdbcTemplate, tableName)
            }
        } else {
            val sqlUnionQuery = sqlSelectQuery as SQLUnionQuery
            if (sqlUnionQuery.limit == null) {
                noLimit = true
                sqlUnionQuery.limit = SQLLimit(reqVo.pageSize!!)
            }
        }
        newSql = SQLUtils.toSQLString(sqlStatement, getDbType())

        return Pair.of(newSql, Triple.of(noLimit, tableName, tablePrimaryKeys))
    }

    private fun getColumnNames(
        records: List<MutableMap<String, Any>>,
        sqlSelectQuery: SQLSelectQuery
    ): List<String> {
        val sqlSelectQueryBlock = sqlSelectQuery as SQLSelectQueryBlock
        val selectList = sqlSelectQueryBlock.selectList
        val columnNames = if (selectList.size == 1 && selectList[0].expr.toString().contains("*")) {
            ArrayList(records[0].keys)
        } else {
            sqlSelectQueryBlock.selectList.stream()
                .map { item: SQLSelectItem ->
                    val expr = item.expr as SQLIdentifierExpr
                    expr.name
                }
                .collect(Collectors.toList())
        }
        return columnNames
    }

    private fun entryToCondition(entry: Map.Entry<String, Any?>): String {
        val key = entry.key
        return when (val value = entry.value) {
            is Date -> {
                key + "=" + String.format("'%s'", value)
            }

            is String -> {
                key + "=" + String.format("'%s'", value)
            }

            else -> {
                "$key=$value"
            }
        }
    }

    override fun getTableRecordTotal(reqVo: CommonErdVo, dbsBean: DbsBean, tokenVo: TokenVo): Int {
        val start = System.currentTimeMillis()

        val jdbcTemplate = jdbcTemplateSingleton(dbsBean.properties!!, reqVo.selectDB!!)

        val sql = reqVo.sql!!.replace(";", "")

        val countSql: String

        val tableName = tryGetTableNameIfSingleTableQuery(sql)
        if (StringUtils.isNotBlank(tableName)) {
            val fromIndex = sql.lowercase(Locale.getDefault()).indexOf("from")
            countSql = "select count(*) " + sql.substring(fromIndex)
        } else {
            countSql = "select count(*) from ($sql) tmp"
        }

        log.info("{}-{} execute count sql:\n{}", tokenVo.userId, tokenVo.username, countSql)
        val count = jdbcTemplate.queryForObject(countSql, Int::class.java)!!

        addSqlToExecuteHistory(
            countSql,
            jdbcTemplate,
            tokenVo,
            reqVo.queryId!!,
            emptyList(),
            System.currentTimeMillis() - start
        )
        return count
    }

    private fun addSqlToExecuteHistory(
        sql: String, jdbcTemplate: JdbcTemplate, tokenVo: TokenVo, queryId: String,
        sqlValues: List<Pair<Any, JdbcType>>, duration: Long
    ) {
        startAsyncTask {
            val dataSource = jdbcTemplate.dataSource as HikariDataSource
            val urlDbName = resolveUrlDbName(dataSource.jdbcUrl)
            val key: String = ERD_PREFIX + "sqlHistory:" + queryId
            val bean = ExecuteHistoryBean()
            bean.sqlInfo = sql
            bean.dbName = urlDbName
            bean.duration = duration
            bean.createTime = Date()
            bean.creator = tokenVo.userId
            bean.params = sqlValues.toString()
            @Suppress("UNCHECKED_CAST")
            val redisTemplateJackson = SpringUtils.context
                .getBean("redisTemplateJackson") as RedisTemplate<String, Any>
            redisTemplateJackson.opsForList().leftPush(key, bean)
            val size = redisTemplateJackson.opsForList().size(key)
            if (size!! > 500) {
                redisTemplateJackson.opsForList().rightPop(key)
            }
        }
    }

    companion object {
        private val typeHandlerRegistry = TypeHandlerRegistry()
        private const val KEY: String = "key"
        private const val INDEX: String = "index"
        private const val NULL_VALUE: String = "<null>"
        private const val ROW_OPERATION_TYPE: String = "rowOperationType"
    }
}
