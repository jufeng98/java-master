package org.javamaster.invocationlab.admin.util;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.IndexsBean;
import org.javamaster.invocationlab.admin.model.erd.PropertiesBean;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yudong
 * @date 2023/2/15
 */
public class DbUtils {
    public static String resolveUrlDbName(String url) {
        URI uri = URI.create(url.substring(5));
        return uri.getPath().substring(1);
    }

    @SuppressWarnings("DataFlowIssue")
    public static String resolveUrlDbName(JdbcTemplate jdbcTemplate) {
        HikariDataSource dataSource = (HikariDataSource) jdbcTemplate.getDataSource();
        return resolveUrlDbName(dataSource.getJdbcUrl());
    }

    private static HikariDataSource newBasicDataSource(PropertiesBean properties, String dbName) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(properties.getDriver_class_name());

        String urlDbName = resolveUrlDbName(properties.getUrl());
        String url = properties.getUrl().replace(urlDbName, dbName);

        dataSource.setJdbcUrl(url);
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setConnectionTestQuery("select 1");
        dataSource.setConnectionInitSql("select 1");
        dataSource.setConnectionTimeout(20000);
        dataSource.setValidationTimeout(3000);
        dataSource.setMaximumPoolSize(6);
        return dataSource;
    }

    public static DataSource dataSourceSingleton(PropertiesBean properties, String dbName) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext().getAutowireCapableBeanFactory();
        String uniqueKey = properties.unique() + ":" + dbName + ":dataSource";
        if (beanFactory.containsBean(uniqueKey)) {
            return (DataSource) beanFactory.getBean(uniqueKey);
        }
        HikariDataSource dataSource = newBasicDataSource(properties, dbName);
        beanFactory.registerSingleton(uniqueKey, dataSource);
        return dataSource;
    }

    public static JdbcTemplate jdbcTemplateSingleton(PropertiesBean properties, String dbName) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext().getAutowireCapableBeanFactory();
        String uniqueKey = properties.unique() + ":" + dbName + ":jdbcTemplate";
        if (beanFactory.containsBean(uniqueKey)) {
            return (JdbcTemplate) beanFactory.getBean(uniqueKey);
        }
        DataSource dataSource = dataSourceSingleton(properties, dbName);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        beanFactory.registerSingleton(uniqueKey, jdbcTemplate);
        return jdbcTemplate;
    }

    public static TransactionTemplate transactionTemplateSingleton(PropertiesBean properties, String dbName) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext().getAutowireCapableBeanFactory();
        String uniqueKey = properties.unique() + ":" + dbName + ":transactionTemplate";
        if (beanFactory.containsBean(uniqueKey)) {
            return (TransactionTemplate) beanFactory.getBean(uniqueKey);
        }
        DataSource dataSource = dataSourceSingleton(properties, dbName);
        PlatformTransactionManager manager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(manager);
        beanFactory.registerSingleton(uniqueKey, transactionTemplate);
        return transactionTemplate;
    }

    public static @NotNull DbsBean getDefaultDb(ErdOnlineModel erdOnlineModel) {
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
        return Objects.requireNonNull(dbsBean);
    }

    @SneakyThrows
    public static List<Table> getTables(JdbcTemplate jdbcTemplate) {
        List<Table> tables = Lists.newArrayList();

        jdbcTemplate.execute((ConnectionCallback<Object>) con -> {
            DatabaseMetaData databaseMetaData = con.getMetaData();
            try (ResultSet rs = databaseMetaData.getTables(null, null, null,
                    new String[]{"TABLE"})) {
                while (rs.next()) {
                    String viewName = rs.getString("TABLE_NAME");
                    String remarks = rs.getString("REMARKS");
                    tables.add(new Table(viewName, StringUtils.defaultString(remarks)));
                }
            }
            return null;
        });

        return tables;
    }

    @SneakyThrows
    public static Table getTableInfo(String tableName, DatabaseMetaData databaseMetaData) {
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
            while (result.next()) {
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
