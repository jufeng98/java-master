package org.javamaster.invocationlab.admin.service.impl

import org.javamaster.invocationlab.admin.model.erd.ApplyBean
import org.javamaster.invocationlab.admin.model.erd.Column
import org.javamaster.invocationlab.admin.util.DbUtils.resolveUrlDbName
import com.alibaba.druid.DbType
import org.apache.commons.lang3.tuple.Pair
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors


class MySqlDbServiceImpl : AbstractDbService() {
    override fun getDbNames(jdbcTemplate: JdbcTemplate?): List<String> {
        val defaultDbName = resolveUrlDbName(jdbcTemplate!!)

        val dbs = jdbcTemplate.queryForList("show databases").stream()
            .map { map: Map<String?, Any> -> map["Database"].toString() }
            .collect(Collectors.toList())
        dbs.remove(defaultDbName)
        dbs.add(0, defaultDbName)
        return dbs
    }

    override fun datatypePredicate(): Predicate<Pair<ApplyBean, Column>> {
        return Predicate { pair: Pair<ApplyBean, Column> ->
            pair.left.mYSQL!!.type == pair.right.typeName!!.uppercase(
                Locale.getDefault()
            )
        }
    }

    override fun getExplainSql(querySql: String): String {
        return "desc $querySql"
    }

    override fun getTableDdlSql(jdbcTemplate: JdbcTemplate, tableName: String): String {
        val list = jdbcTemplate.queryForList("show create table $tableName")
        return list[0]["Create Table"].toString()
    }

    override fun getDbType(): DbType {
        return DbType.mysql
    }
}
