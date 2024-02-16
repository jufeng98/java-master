package org.javamaster.invocationlab.admin.util

import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.model.erd.*
import com.google.common.collect.Lists
import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.net.URI
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.util.stream.Collectors
import javax.sql.DataSource

/**
 * @author yudong
 * @date 2023/2/15
 */
object DbUtils {
    @JvmStatic
    fun resolveUrlDbName(url: String): String {
        val uri = URI.create(url.substring(5))
        return uri.path.substring(1)
    }

    @JvmStatic
    fun resolveUrlDbName(jdbcTemplate: JdbcTemplate): String {
        val dataSource = jdbcTemplate.dataSource as HikariDataSource
        return resolveUrlDbName(dataSource.jdbcUrl)
    }

    private fun newBasicDataSource(properties: PropertiesBean, dbName: String): HikariDataSource {
        val dataSource = HikariDataSource()
        dataSource.driverClassName = properties.driver_class_name

        val urlDbName = resolveUrlDbName(properties.url!!)
        val url = properties.url!!.replace(urlDbName, dbName)

        dataSource.jdbcUrl = url
        dataSource.username = properties.username
        dataSource.password = properties.password
        dataSource.connectionTestQuery = "select 1"
        dataSource.connectionInitSql = "select 1"
        dataSource.connectionTimeout = 20000
        dataSource.validationTimeout = 3000
        dataSource.maximumPoolSize = 6
        return dataSource
    }

    private fun dataSourceSingleton(properties: PropertiesBean, dbName: String): DataSource {
        val beanFactory = SpringUtils.context.autowireCapableBeanFactory as DefaultListableBeanFactory
        val uniqueKey = properties.unique() + ":" + dbName + ":dataSource"
        if (beanFactory.containsBean(uniqueKey)) {
            return beanFactory.getBean(uniqueKey) as DataSource
        }
        val dataSource = newBasicDataSource(properties, dbName)
        beanFactory.registerSingleton(uniqueKey, dataSource)
        return dataSource
    }

    @JvmStatic
    fun jdbcTemplateSingleton(properties: PropertiesBean, dbName: String): JdbcTemplate {
        val beanFactory = SpringUtils.context.autowireCapableBeanFactory as DefaultListableBeanFactory
        val uniqueKey = properties.unique() + ":" + dbName + ":jdbcTemplate"
        if (beanFactory.containsBean(uniqueKey)) {
            return beanFactory.getBean(uniqueKey) as JdbcTemplate
        }
        val dataSource = dataSourceSingleton(properties, dbName)
        val jdbcTemplate = JdbcTemplate(dataSource)
        beanFactory.registerSingleton(uniqueKey, jdbcTemplate)
        return jdbcTemplate
    }

    @JvmStatic
    fun transactionTemplateSingleton(properties: PropertiesBean, dbName: String): TransactionTemplate {
        val beanFactory = SpringUtils.context.autowireCapableBeanFactory as DefaultListableBeanFactory
        val uniqueKey = properties.unique() + ":" + dbName + ":transactionTemplate"
        if (beanFactory.containsBean(uniqueKey)) {
            return beanFactory.getBean(uniqueKey) as TransactionTemplate
        }
        val dataSource = dataSourceSingleton(properties, dbName)
        val manager: PlatformTransactionManager = DataSourceTransactionManager(dataSource)
        val transactionTemplate = TransactionTemplate(manager)
        beanFactory.registerSingleton(uniqueKey, transactionTemplate)
        return transactionTemplate
    }

    @JvmStatic
    fun getDefaultDb(erdOnlineModel: ErdOnlineModel): DbsBean {
        val dbs = erdOnlineModel.projectJSON!!.profile!!.dbs ?: throw ErdException("请先保存并设置默认数据源")
        var dbsBean: DbsBean? = null
        for (db in dbs) {
            if (db.defaultDB!!) {
                dbsBean = db
            }
        }
        return dbsBean!!
    }

    @JvmStatic

    fun getTables(jdbcTemplate: JdbcTemplate): List<Table> {
        val tables: MutableList<Table> = Lists.newArrayList()

        jdbcTemplate.execute<Any> { con: Connection ->
            val databaseMetaData = con.metaData
            databaseMetaData.getTables(
                null, null, null,
                arrayOf("TABLE")
            ).use { rs ->
                while (rs.next()) {
                    val viewName = rs.getString("TABLE_NAME")
                    val remarks = rs.getString("REMARKS")
                    tables.add(Table(viewName, StringUtils.defaultString(remarks)))
                }
            }
            null
        }

        return tables
    }

    @JvmStatic

    fun getTableInfo(tableName: String, databaseMetaData: DatabaseMetaData): Table? {
        var tables: MutableList<Table>
        databaseMetaData.getTables(null, null, tableName, arrayOf("TABLE")).use { rs ->
            tables = Lists.newArrayList()
            while (rs.next()) {
                val viewName = rs.getString("TABLE_NAME")
                val remarks = rs.getString("REMARKS")
                tables.add(Table(viewName, StringUtils.defaultString(remarks)))
            }
        }
        if (tables.isEmpty()) {
            return null
        }
        return tables[0]
    }

    @JvmStatic

    fun getTableIndexes(tableName: String, databaseMetaData: DatabaseMetaData): List<IndexsBean> {
        var indexsBeans: MutableList<IndexsBean>
        databaseMetaData.getIndexInfo(null, null, tableName, false, false).use { rs ->
            indexsBeans = Lists.newArrayList()
            val multiValueMap: MultiValueMap<String, String> = LinkedMultiValueMap()
            while (rs.next()) {
                val columnName = rs.getString("COLUMN_NAME")
                val nonUnique = rs.getBoolean("NON_UNIQUE")
                val indexName = rs.getString("INDEX_NAME")
                val indexsBean = IndexsBean()
                indexsBean.name = indexName
                indexsBean.isUnique = !nonUnique
                multiValueMap.add(indexName, columnName)
                indexsBean.fields = multiValueMap[indexName]
                indexsBeans.add(indexsBean)
            }
            indexsBeans = indexsBeans.stream().distinct().collect(Collectors.toList())
        }
        return indexsBeans
    }

    @JvmStatic
    fun getTableColumns(jdbcTemplate: JdbcTemplate, tableName: String): List<Column> {
        return jdbcTemplate.execute { con: Connection ->
            val databaseMetaData = con.metaData
            getTableColumns(tableName, databaseMetaData)
        }!!
    }

    @JvmStatic
    fun getTableColumns(tableName: String, databaseMetaData: DatabaseMetaData): List<Column> {
        val primaryColumnNames: MutableList<String> = Lists.newArrayList()
        databaseMetaData.getPrimaryKeys(null, null, tableName).use { result ->
            while (result.next()) {
                primaryColumnNames.add(result.getString("COLUMN_NAME"))
            }
        }
        var columns: MutableList<Column>
        databaseMetaData.getColumns(null, null, tableName, null).use { rs ->
            columns = Lists.newArrayList()
            while (rs.next()) {
                val column = Column()
                val columnName = rs.getString("COLUMN_NAME")
                if (primaryColumnNames.contains(columnName)) {
                    column.primaryKey = true
                }
                column.name = columnName
                val dataType = rs.getInt("DATA_TYPE")
                column.type = dataType
                val dataTypeName = rs.getString("TYPE_NAME")
                column.typeName = dataTypeName
                val columnSize = rs.getInt("COLUMN_SIZE")
                column.size = columnSize
                val decimalDigits = rs.getInt("DECIMAL_DIGITS")
                column.digits = decimalDigits
                val numPrecRadix = rs.getInt("NUM_PREC_RADIX")
                column.precRadix = numPrecRadix
                val nullAble = rs.getInt("NULLABLE")
                column.nullable = nullAble
                val remarks = rs.getString("REMARKS")
                column.remarks = StringUtils.defaultString(remarks)
                val columnDef = rs.getString("COLUMN_DEF")
                column.def = columnDef
                val charOctetLength = rs.getInt("CHAR_OCTET_LENGTH")
                column.charOctetLength = charOctetLength
                val ordinalPosition = rs.getInt("ORDINAL_POSITION")
                column.ordinalPosition = ordinalPosition
                val isNullAble = rs.getString("IS_NULLABLE")
                column.isNullable = isNullAble
                val isAutoincrement = rs.getString("IS_AUTOINCREMENT")
                column.isAutoincrement = isAutoincrement
                columns.add(column)
            }
        }
        return columns
    }
}
