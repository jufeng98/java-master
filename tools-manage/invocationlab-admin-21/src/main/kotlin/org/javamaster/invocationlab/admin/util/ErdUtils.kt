package org.javamaster.invocationlab.admin.util

import org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX
import org.javamaster.invocationlab.admin.enums.ProjectType
import org.javamaster.invocationlab.admin.model.erd.*
import org.javamaster.invocationlab.admin.model.erd.ApplyBean.MYSQLBean
import org.javamaster.invocationlab.admin.util.DbUtils.getTableColumns
import org.javamaster.invocationlab.admin.util.DbUtils.getTableIndexes
import org.javamaster.invocationlab.admin.util.DbUtils.getTableInfo
import com.google.common.collect.Lists
import org.apache.commons.lang3.tuple.Pair
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.DatabaseMetaData
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * @author yudong
 * @date 2023/2/12
 */

object ErdUtils {
    val log: Logger = LoggerFactory.getLogger(ErdUtils.javaClass)

    fun tableToEntity(
        entitiesBean: EntitiesBean, databaseMetaData: DatabaseMetaData,
        datatypeBeans: List<DatatypeBean>,
        predicate: Predicate<Pair<ApplyBean, Column>>
    ): EntitiesBean {
        val tableName = entitiesBean.title!!
        val table = getTableInfo(tableName, databaseMetaData) ?: return entitiesBean
        entitiesBean.chnname = table.remarks

        val columns = getTableColumns(tableName, databaseMetaData)
        val fieldsBeans = toFieldsBeans(columns, datatypeBeans, predicate)

        entitiesBean.fields = fieldsBeans

        val indexesBeans = getTableIndexes(tableName, databaseMetaData)
        entitiesBean.indexs = indexesBeans

        return entitiesBean
    }

    private fun toFieldsBeans(
        columns: List<Column>, datatypeBeans: List<DatatypeBean>,
        predicate: Predicate<Pair<ApplyBean, Column>>
    ): List<FieldsBean> {
        return columns.stream()
            .map { column: Column ->
                val fieldsBean = FieldsBean()
                fieldsBean.chnname = column.remarks
                fieldsBean.name = column.name

                val datatypeBean = findDatatypeBean(datatypeBeans, column, predicate)

                fieldsBean.typeName = datatypeBean.name
                fieldsBean.type = datatypeBean.code
                fieldsBean.dataType = datatypeBean.name
                fieldsBean.rawDataType = column.typeName
                if (datatypeBean.code!!.contains("Date")) {
                    fieldsBean.remark = ""
                } else {
                    val sizes: MutableList<String> = Lists.newArrayList(
                        column.size.toString() + ""
                    )
                    if (column.digits != 0) {
                        sizes.add(column.digits.toString() + "")
                    }
                    val size = java.lang.String.join(",", sizes)
                    fieldsBean.remark = "($size)"
                }
                fieldsBean.pk = column.primaryKey
                fieldsBean.notNull = "NO" == column.isNullable
                fieldsBean.autoIncrement = "YES" == column.isAutoincrement
                fieldsBean.relationNoShow = false
                fieldsBean.defaultValue = column.def
                fieldsBean.uiHint = ""
                fieldsBean
            }
            .collect(Collectors.toList())
    }

    private fun findDatatypeBean(
        datatypeBeans: List<DatatypeBean>, column: Column,
        predicate: Predicate<Pair<ApplyBean, Column>>
    ): DatatypeBean {
        val list = datatypeBeans.stream()
            .filter { datatypeBean: DatatypeBean -> predicate.test(Pair.of(datatypeBean.apply, column)) }
            .collect(Collectors.toList())
        if (list.isEmpty()) {
            log.error("type not exists:{}", column)
            val datatypeBean = DatatypeBean()
            datatypeBean.name = column.typeName
            datatypeBean.code = column.typeName
            val applyBean = ApplyBean()
            val mysqlBean = MYSQLBean()
            mysqlBean.type = column.typeName
            applyBean.mYSQL = mysqlBean
            datatypeBean.apply = applyBean
            return datatypeBean
        }
        return list[0]
    }

    @JvmStatic
    fun findModulesBean(moduleCode: String, erdOnlineModel: ErdOnlineModel): ModulesBean {
        var module: ModulesBean? = null
        for (modulesBean in erdOnlineModel.projectJSON!!.modules!!) {
            if (modulesBean.name == moduleCode) {
                module = modulesBean
                break
            }
        }
        return module!!
    }

    @JvmStatic
    fun getPair(projectType: ProjectType, userId: String): Pair<String, String> {
        val name = projectType.name.lowercase(Locale.getDefault())
        val prefix: String = ERD_PREFIX + "page:" + name
        val key = "$userId:$name"
        return Pair.of(prefix, key)
    }
}
