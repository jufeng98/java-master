package org.javamaster.invocationlab.admin.util;

import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.IndexsBean;
import org.javamaster.invocationlab.admin.model.erd.PropertiesBean;
import org.javamaster.invocationlab.admin.model.erd.Table;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author yudong
 * @date 2023/2/15
 */
public class DbUtils {
    static BasicDataSource basicDataSource(PropertiesBean properties) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(properties.getDriver_class_name());
        dataSource.setUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setInitialSize(1);
        dataSource.setMinIdle(1);
        dataSource.setDefaultAutoCommit(true);
        dataSource.setTestOnBorrow(true);
        return dataSource;
    }

    public static JdbcTemplate jdbcTemplate(PropertiesBean properties) {
        BasicDataSource dataSource = basicDataSource(properties);
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext().getAutowireCapableBeanFactory();
        String unique = properties.unique();
        if (beanFactory.containsBean(unique)) {
            return (JdbcTemplate) beanFactory.getBean(unique);
        }
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        beanFactory.registerSingleton(unique, jdbcTemplate);
        return jdbcTemplate;
    }

    public static DbsBean getDefaultDb(ErdOnlineModel erdOnlineModel) {
        List<DbsBean> dbs = erdOnlineModel.getProjectJSON().getProfile().getDbs();
        if (dbs == null) {
            throw new ErdException("请先保存并设置默认数据源");
        }
        DbsBean dbsBean = null;
        for (DbsBean db : dbs) {
            if (db.getDefaultDB()) {
                dbsBean = db;
            }
        }
        return dbsBean;
    }

    @SneakyThrows
    public static void checkDb(DbsBean dbsBean) {
        if (!"MYSQL".equals(dbsBean.getSelect())) {
            throw new ErdException("目前只支持MySQL");
        }
        try {
            JdbcTemplate jdbcTemplate = jdbcTemplate(dbsBean.getProperties());
            jdbcTemplate.execute("select now()");
        } catch (Exception e) {
            throw new ErdException(e.getMessage());
        }
    }

    public static String getTableName(PlainSelect plainSelect) {
        net.sf.jsqlparser.schema.Table table = (net.sf.jsqlparser.schema.Table) plainSelect.getFromItem();
        return table.getName();
    }

    public static Pair<String, List<String>> getTableNameAndPrimaryKey(JdbcTemplate jdbcTemplate, String sql) {
        PlainSelect plainSelect;
        try {
            plainSelect = parseSql(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        net.sf.jsqlparser.schema.Table table = (net.sf.jsqlparser.schema.Table) plainSelect.getFromItem();
        String tableName = table.getName();
        List<String> tablePrimaryKeys = DbUtils.getTablePrimaryColumns1(jdbcTemplate, tableName).stream()
                .map(Column::getName)
                .collect(Collectors.toList());
        return Pair.of(tableName, tablePrimaryKeys);
    }

    public static PlainSelect parseSql(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        return (PlainSelect) select.getSelectBody();
    }

    @SneakyThrows
    static Table getTableInfo(String tableName, DatabaseMetaData databaseMetaData) {
        List<Table> tables;
        try (ResultSet rs = databaseMetaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            tables = Lists.newArrayList();
            while (rs.next()) {
                String viewName = rs.getString("TABLE_NAME");
                String remarks = rs.getString("REMARKS");
                tables.add(new Table(viewName, StringUtils.defaultString(remarks)));
            }
        }
        if (tables.isEmpty()) {
            return null;
        }
        return tables.get(0);
    }

    @SneakyThrows
    static List<IndexsBean> getTableIndexes(String tableName, DatabaseMetaData databaseMetaData) {
        List<IndexsBean> indexsBeans;
        try (ResultSet rs = databaseMetaData.getIndexInfo(null, null, tableName, false, false)) {
            indexsBeans = Lists.newArrayList();
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                boolean nonUnique = rs.getBoolean("NON_UNIQUE");
                String indexName = rs.getString("INDEX_NAME");
                IndexsBean indexsBean = new IndexsBean();
                indexsBean.setName(indexName);
                indexsBean.setIsUnique(!nonUnique);
                multiValueMap.add(indexName, columnName);
                indexsBean.setFields(multiValueMap.get(indexName));
                indexsBeans.add(indexsBean);
            }
            indexsBeans = indexsBeans.stream().distinct().collect(Collectors.toList());
        }
        return indexsBeans;
    }

    public static <T> T executeAndResetDefaultDb(JdbcTemplate jdbcTemplate, String selectDB, Supplier<T> supplier) {
        String dbName = (String) jdbcTemplate.queryForMap("select database()").get("database()");
        try {
            jdbcTemplate.execute("use " + selectDB);
            return supplier.get();
        } finally {
            jdbcTemplate.execute("use " + dbName);
        }
    }

    public static List<Column> getTablePrimaryColumns1(JdbcTemplate jdbcTemplate, String tableName) {
        List<Column> tableColumns = getTableColumns1(jdbcTemplate, tableName);
        return tableColumns.stream().filter(Column::isPrimaryKey).collect(Collectors.toList());
    }

    public static List<Column> getTableColumns1(JdbcTemplate jdbcTemplate, String tableName) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList("show full columns from " + tableName);
        return list.stream()
                .map(map -> {
                    Column column = new Column();
                    column.setName(map.get("Field").toString());
                    column.setTypeName(map.get("Type").toString());
                    column.setRemarks(map.get("Comment").toString());
                    column.setIsNullable(map.get("Null").toString());
                    column.setPrimaryKey("PRI".equals(map.get("Key")));
                    column.setDef(map.get("Default") != null ? map.get("Default").toString() : "");
                    Object extra = map.get("Extra");
                    if (extra != null && extra.equals("auto_increment")) {
                        column.setIsAutoincrement("YES");
                    }
                    return column;
                })
                .collect(Collectors.toList());
    }

    public static List<Column> getTablePrimaryColumns(JdbcTemplate jdbcTemplate, String tableName) {
        List<Column> tableColumns = getTableColumns(jdbcTemplate, tableName);
        return tableColumns.stream().filter(Column::isPrimaryKey).collect(Collectors.toList());
    }

    public static List<Column> getTableColumns(JdbcTemplate jdbcTemplate, String tableName) {
        return jdbcTemplate.execute((ConnectionCallback<List<Column>>) con -> {
            DatabaseMetaData databaseMetaData = con.getMetaData();
            return getTableColumns(tableName, databaseMetaData);
        });
    }

    @SneakyThrows
    static List<Column> getTableColumns(String tableName, DatabaseMetaData databaseMetaData) {
        List<String> primaryColumnNames = Lists.newArrayList();
        try (ResultSet result = databaseMetaData.getPrimaryKeys(null, null, tableName)) {
            if (result.next()) {
                primaryColumnNames.add(result.getString("COLUMN_NAME"));
            }
        }
        List<Column> columns;
        try (ResultSet rs = databaseMetaData.getColumns(null, null, tableName, null)) {
            columns = Lists.newArrayList();
            while (rs.next()) {
                Column column = new Column();
                String columnName = rs.getString("COLUMN_NAME");
                if (primaryColumnNames.contains(columnName)) {
                    column.setPrimaryKey(true);
                }
                column.setName(columnName);
                int dataType = rs.getInt("DATA_TYPE");
                column.setType(dataType);
                String dataTypeName = rs.getString("TYPE_NAME");
                column.setTypeName(dataTypeName);
                int columnSize = rs.getInt("COLUMN_SIZE");
                column.setSize(columnSize);
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                column.setDigits(decimalDigits);
                int numPrecRadix = rs.getInt("NUM_PREC_RADIX");
                column.setPrecRadix(numPrecRadix);
                int nullAble = rs.getInt("NULLABLE");
                column.setNullable(nullAble);
                String remarks = rs.getString("REMARKS");
                column.setRemarks(StringUtils.defaultString(remarks));
                String columnDef = rs.getString("COLUMN_DEF");
                column.setDef(columnDef);
                int charOctetLength = rs.getInt("CHAR_OCTET_LENGTH");
                column.setCharOctetLength(charOctetLength);
                int ordinalPosition = rs.getInt("ORDINAL_POSITION");
                column.setOrdinalPosition(ordinalPosition);
                String isNullAble = rs.getString("IS_NULLABLE");
                column.setIsNullable(isNullAble);
                String isAutoincrement = rs.getString("IS_AUTOINCREMENT");
                column.setIsAutoincrement(isAutoincrement);
                columns.add(column);
            }
        }
        return columns;
    }

}
