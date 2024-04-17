package org.javamaster.invocationlab.admin.service.impl;

import com.alibaba.druid.DbType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.util.DbUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("PostgreSQL")
public class PostgreSqlDbServiceImpl extends AbstractDbService {

    @Override
    public List<String> getDbNames(JdbcTemplate jdbcTemplate) {
        String defaultDbName = DbUtils.resolveUrlDbName(jdbcTemplate);

        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT datname FROM pg_database");
        List<String> dbs = list.stream()
                .map(it -> it.get("datname").toString())
                .collect(Collectors.toList());
        dbs.remove(defaultDbName);
        dbs.add(0, defaultDbName);
        return dbs;
    }

    @Override
    protected List<Column> getTableColumns(JdbcTemplate jdbcTemplate, String tableName) {
        List<Column> tableColumns = super.getTableColumns(jdbcTemplate, tableName);

        List<String> keys = filterPrimaryKeys(tableColumns);
        if (!keys.isEmpty()) {
            return tableColumns;
        }

        List<Column> dataIdColumns = tableColumns.stream()
                .filter(column -> column.getName().equals("data_id"))
                .collect(Collectors.toList());
        if (dataIdColumns.isEmpty()) {
            return tableColumns;
        }

        // 对于没有主键的表特殊处理,认为data_id就是主键
        dataIdColumns.get(0).setPrimaryKey(true);
        return tableColumns;
    }

    @Override
    @SneakyThrows
    protected String getTableDdlSql(JdbcTemplate jdbcTemplate, String tableName) {
        File file = ResourceUtils.getFile("classpath:script/pg-ddl.sql");
        String sql = new String(Files.readAllBytes(file.toPath())).replace("{tableName}", tableName);
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    @Override
    protected void modifyQueryRow(Map<String, Object> rowMap) {
        for (Map.Entry<String, Object> entry : rowMap.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                Class<?> clazz = value.getClass();
                if (clazz.getSimpleName().toLowerCase().contains("pg")) {
                    rowMap.put(entry.getKey(), value.toString());
                }
            }
        }
    }

    @Override
    public DbType getDbType() {
        return DbType.postgresql;
    }
}
