package org.javamaster.invocationlab.admin.service.impl

import org.javamaster.invocationlab.admin.inteceptor.AppInterceptor
import org.javamaster.invocationlab.admin.model.erd.ApplyBean
import org.javamaster.invocationlab.admin.model.erd.Column
import org.javamaster.invocationlab.admin.util.DbUtils.resolveUrlDbName
import com.alibaba.druid.DbType
import org.apache.commons.lang3.tuple.Pair
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors


class PostgreSqlDbServiceImpl : AbstractDbService() {
    override fun getDbNames(jdbcTemplate: JdbcTemplate?): List<String> {
        val defaultDbName = resolveUrlDbName(jdbcTemplate!!)

        val list = jdbcTemplate.queryForList("SELECT datname FROM pg_database")
        val dbs = list.stream()
            .map { it: Map<String, Any> -> it["datname"].toString() }
            .collect(Collectors.toList())
        dbs.remove(defaultDbName)
        dbs.add(0, defaultDbName)
        return dbs
    }

    override fun getTableColumns(jdbcTemplate: JdbcTemplate, tableName: String): List<Column> {
        val tableColumns = super.getTableColumns(jdbcTemplate, tableName)

        val keys = filterPrimaryKeys(tableColumns)
        if (keys.isNotEmpty()) {
            return tableColumns
        }

        val dataIdColumns = tableColumns.stream()
            .filter { column: Column -> column.name == "data_id" }
            .collect(Collectors.toList())
        if (dataIdColumns.isEmpty()) {
            return tableColumns
        }

        // 对于没有主键的表特殊处理,认为data_id就是主键
        dataIdColumns[0].primaryKey = true
        return tableColumns
    }

    override fun datatypePredicate(): Predicate<Pair<ApplyBean, Column>> {
        return Predicate { pair: Pair<ApplyBean, Column> ->
            pair.left.postgreSQL!!.type == pair.right.typeName!!.uppercase(
                Locale.getDefault()
            )
        }
    }


    override fun getTableDdlSql(jdbcTemplate: JdbcTemplate, tableName: String): String {
        AppInterceptor::class.java.getResourceAsStream("/script/pg-ddl.sql").use {
            val sql = StreamUtils.copyToString(it, StandardCharsets.UTF_8).replace("{tableName}", tableName)
            return jdbcTemplate.queryForObject(sql, String::class.java)!!
        }
    }

    override fun modifyQueryRow(rowMap: MutableMap<String, Any>) {
        for ((key, value) in rowMap) {
            val clazz: Class<*> = value.javaClass
            if (clazz.simpleName.lowercase(Locale.getDefault()).contains("pg")) {
                rowMap[key] = value.toString()
            }
        }
    }

    override fun getDbType(): DbType {
        return DbType.postgresql
    }
}
