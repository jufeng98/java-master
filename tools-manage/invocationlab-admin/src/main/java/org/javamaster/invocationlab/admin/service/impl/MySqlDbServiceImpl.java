package org.javamaster.invocationlab.admin.service.impl;

import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.enums.SqlTypeEnum;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.CommonErdVo;
import org.javamaster.invocationlab.admin.model.erd.DatatypeBean;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.EntitiesBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.SqlExecResVo;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.javamaster.invocationlab.admin.model.erd.TableData;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.util.DbUtils;
import org.javamaster.invocationlab.admin.util.ErdUtils;
import org.javamaster.invocationlab.admin.util.SessionUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.consts.ErdConst.QUERY_RES;
import static org.javamaster.invocationlab.admin.util.DbUtils.getDefaultDb;
import static org.javamaster.invocationlab.admin.util.DbUtils.getTableName;
import static org.javamaster.invocationlab.admin.util.DbUtils.jdbcTemplateSingleton;
import static org.javamaster.invocationlab.admin.util.JsonUtils.STANDARD_PATTERN;

@Slf4j
public class MySqlDbServiceImpl extends AbstractDbService {

    @Override
    public String getCheckSql() {
        return "select now()";
    }

    @Override
    public List<String> getDbs(JdbcTemplate jdbcTemplate) {
        String defaultDbName = (String) jdbcTemplate.queryForMap("select database()").get("database()");
        List<String> dbs = jdbcTemplate.queryForList("show databases").stream()
                .map(map -> map.get("Database").toString())
                .collect(Collectors.toList());
        dbs.remove(defaultDbName);
        dbs.add(0, defaultDbName);
        return dbs;
    }

    @Override
    public List<Table> getTables(JdbcTemplate jdbcTemplate, String selectDB) {
        String sql = "select table_name,table_comment from information_schema.tables where table_schema = '" + selectDB + "'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return list.stream()
                .map(map -> {
                    String tableName = (String) map.get("table_name");
                    String remarks = (String) map.get("table_comment");
                    return new Table(tableName, remarks);
                })
                .collect(Collectors.toList());
    }

    @Override
    protected List<Column> getPrimaryColumns(JdbcTemplate jdbcTemplate, String selectDB, String tableName) {
        List<Column> tableColumns = getTableColumns(jdbcTemplate, selectDB, tableName);
        return tableColumns.stream()
                .filter(Column::isPrimaryKey)
                .collect(Collectors.toList());
    }

    @Override
    protected List<String> getPrimaryKeys(JdbcTemplate jdbcTemplate, String selectDB, String tableName) {
        List<Column> tableColumns = getPrimaryColumns(jdbcTemplate, selectDB, tableName);
        return tableColumns.stream()
                .map(Column::getName)
                .collect(Collectors.toList());
    }

    @Override
    protected Pair<String, List<Object>> handleDeleteOperation(Pair<String, List<Object>> pairPrimary, JSONObject row, String tableName) {
        String sql;
        List<Object> fieldValues = Lists.newArrayList();
        boolean noPrimaryKeyInRequest = pairPrimary.getValue().stream().anyMatch(Objects::isNull);
        if (noPrimaryKeyInRequest) {
            Map<String, Object> oldRow = getOldQueryMap(row.get(KEY));
            Objects.requireNonNull(oldRow).remove(KEY);
            sql = String.format("delete from %s where %s", tableName,
                    oldRow.keySet().stream()
                            .map(key -> key + "=?")
                            .collect(Collectors.joining(" AND "))
            );
            fieldValues.addAll(oldRow.values());
        } else {
            sql = String.format("delete from %s where %s", tableName, pairPrimary.getLeft());
            fieldValues.addAll(pairPrimary.getRight());
        }
        return Pair.of(sql, fieldValues);
    }

    @Override
    protected Pair<String, List<Object>> handleEditOperation(Pair<String, List<Object>> pairPrimary, JSONObject row, String tableName) {
        String sql;
        List<Object> fieldValues = Lists.newArrayList();
        Map<String, Object> oldRow = getOldQueryMap(row.get(KEY));
        if (oldRow == null) {
            throw new ErdException("请重新刷新查询后再重试");
        }
        String fields = row.entrySet().stream()
                .filter(this::filterUnRelateColumn)
                .filter(entry -> filterUnChangeColumn(entry, oldRow))
                .map(entry -> {
                    String columnName = entry.getKey();
                    Object columnValue = entry.getValue();
                    if (NULL_VALUE.equals(columnValue)) {
                        fieldValues.add(null);
                    } else {
                        fieldValues.add(columnValue);
                    }
                    return columnName + "=?";
                })
                .collect(Collectors.joining(","));
        if (StringUtils.isBlank(fields)) {
            throw new ErdException("数据无变化,未做任何更新操作!");
        }
        boolean noPrimaryKeyInRequest = pairPrimary.getValue().stream().anyMatch(Objects::isNull);
        if (noPrimaryKeyInRequest) {
            Objects.requireNonNull(oldRow).remove(KEY);
            sql = String.format("update %s set %s where %s", tableName, fields,
                    oldRow.keySet().stream().map(key -> key + "=?").collect(Collectors.joining(" AND ")));
            fieldValues.addAll(oldRow.values());
        } else {
            sql = String.format("update %s set %s where %s", tableName, fields, pairPrimary.getKey());
            fieldValues.addAll(pairPrimary.getValue());
        }
        return Pair.of(sql, fieldValues);
    }

    @Override
    protected Pair<String, List<Object>> handleInsertOperation(JSONObject row, String tableName) {
        List<Object> fieldValues = Lists.newArrayList();
        List<String> names = Lists.newArrayList();
        row.entrySet().stream()
                .filter(this::filterUnRelateColumn)
                .forEach(entry -> {
                    names.add(entry.getKey());
                    if (NULL_VALUE.equals(entry.getValue())) {
                        fieldValues.add(null);
                    } else {
                        fieldValues.add(entry.getValue());
                    }
                });
        String sql = String.format("insert into %s (%s) values (%s)", tableName, String.join(",", names), names.stream()
                .map(name -> "?")
                .collect(Collectors.joining(",")));
        return Pair.of(sql, fieldValues);
    }

    @Override
    public List<Column> getTableColumns(JdbcTemplate jdbcTemplate, String selectDB, String tableName) {
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

    @Override
    protected SqlExecResVo executeExplainSql(CommonErdVo reqVo, JdbcTemplate jdbcTemplate, TokenVo tokenVo) {
        String tmpSql = "desc " + reqVo.getSql();
        long start = System.currentTimeMillis();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(tmpSql);
        long end = System.currentTimeMillis();

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setColumns(list.get(0).keySet());

        TableData tableData = new TableData();
        tableData.setTotal(1);
        tableData.setRecords(list);
        resVo.setTableData(tableData);

        log.info("{}-{} execute explain sql:{}", tokenVo.getUserId(), tokenVo.getUsername(), tmpSql);
        addSqlToExecuteHistory(tmpSql, jdbcTemplate, tokenVo, reqVo.getQueryId(), Collections.emptyList(), end - start);
        return resVo;
    }

    @Override
    protected SqlExecResVo executeDangerousSql(JdbcTemplate jdbcTemplate, TokenVo tokenVo, boolean ddlSql, CommonErdVo reqVo) {
        List<String> sqlList = Arrays.stream(reqVo.getSql().split(";")).collect(Collectors.toList());
        if (!ddlSql) {
            List<String> dangerSqlList = sqlList.stream()
                    .filter(tmpSql -> {
                        String lowerSql = tmpSql.toLowerCase().replaceAll("\\s", " ").trim();
                        return !lowerSql.startsWith(SqlTypeEnum.INSERT.type) && !lowerSql.contains("where");
                    })
                    .collect(Collectors.toList());
            if (!dangerSqlList.isEmpty()) {
                throw new ErdException(String.join("、", dangerSqlList) + " 语句缺少where条件!");
            }
        }

        log.info("{}-{} execute dml sql:\n{}", tokenVo.getUserId(), tokenVo.getUsername(), reqVo.getSql());
        long start = System.currentTimeMillis();
        int[] num = jdbcTemplate.batchUpdate(sqlList.toArray(new String[0]));
        long end = System.currentTimeMillis();
        log.info("{}-{} execute dml sql res num:{}", tokenVo.getUserId(), tokenVo.getUsername(), Arrays.toString(num));

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setColumns(Sets.newHashSet("affect_num"));
        resVo.setTableColumns(Collections.emptyMap());
        resVo.setPrimaryKeys(Collections.emptyList());
        resVo.setQueryKey(atomicInteger.incrementAndGet());

        TableData tableData = new TableData();
        tableData.setTotal(1);

        Map<String, Object> map = Maps.newHashMap();
        map.put("affect_num", Arrays.stream(num).sum());

        //noinspection unchecked
        tableData.setRecords(Lists.newArrayList(map));

        resVo.setTableData(tableData);

        addSqlToExecuteHistory(reqVo.getSql(), jdbcTemplate, tokenVo, reqVo.getQueryId(), Collections.emptyList(), end - start);
        return resVo;
    }

    @Override
    public Integer getTableRecordTotal(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo) {
        long start = System.currentTimeMillis();

        JdbcTemplate jdbcTemplate = DbUtils.jdbcTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());

        String sql = reqVo.getSql().replace(";", "");
        PlainSelect plainSelect = DbUtils.parseSql(sql);

        Integer count;
        String countSql;
        if (plainSelect.getGroupBy() != null) {
            countSql = "select count(*) from (" + sql + ") tmp";
        } else {
            int fromIndex = sql.toLowerCase().indexOf("from");
            countSql = "select count(*) " + sql.substring(fromIndex);
        }
        count = jdbcTemplate.queryForObject(countSql, Integer.class);

        addSqlToExecuteHistory(countSql, jdbcTemplate, tokenVo, reqVo.getQueryId(), Collections.emptyList(), System.currentTimeMillis() - start);
        return count;
    }

    @Override
    protected SqlExecResVo executeDqlSql(JdbcTemplate jdbcTemplate, TokenVo tokenVo, boolean useNoneDefaultDb, CommonErdVo reqVo) {
        String tmpSql = reqVo.getSql().replace(";", "");
        Pair<String, Triple<Boolean, String, List<String>>> pair = Pair.of(tmpSql,
                Triple.of(false, null, Lists.newArrayList()));

        PlainSelect plainSelect;
        try {
            plainSelect = DbUtils.parseSql(tmpSql);
            pair = modifyQuerySql(plainSelect, tmpSql, jdbcTemplate, reqVo);
        } catch (Exception e) {
            log.warn("modify sql error:{}", e.getMessage());
        }
        tmpSql = pair.getLeft();
        boolean showPagination = pair.getRight().getLeft();
        List<String> primaryKeys = pair.getRight().getRight();

        log.info("{}-{} execute dql sql:{}", tokenVo.getUserId(), tokenVo.getUsername(), tmpSql);
        long start = System.currentTimeMillis();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(tmpSql);
        long end = System.currentTimeMillis();
        log.info("{}-{} execute dql sql res size:{}", tokenVo.getUserId(), tokenVo.getUsername(), list.size());

        Set<String> columns = Sets.newLinkedHashSet();
        String tableName = pair.getRight().getMiddle();
        if (list.isEmpty()) {
            if (!primaryKeys.isEmpty()) {
                List<Column> tableColumns = getTableColumns(jdbcTemplate, reqVo.getSelectDB(), tableName);
                columns.addAll(tableColumns.stream()
                        .map(Column::getName)
                        .collect(Collectors.toList()));
                columns.add(KEY);
            }
        } else {
            columns.addAll(list.get(0).keySet());
            columns.add(KEY);
            int anInt = Integer.parseInt(RandomUtils.nextInt(1000000, 9999999) + "01");
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> rowMap = list.get(i);
                rowMap.put(KEY, anInt + i);
                if (!reqVo.getIsExport()) {
                    modifyRowForSpecialColumnType(rowMap);
                }
            }
        }

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setColumns(columns);
        resVo.setQueryKey(atomicInteger.incrementAndGet());
        resVo.setPage(reqVo.getPage());
        resVo.setPageSize(reqVo.getPageSize());
        resVo.setShowPagination(showPagination);
        resVo.setTableName(tableName);
        resVo.setPrimaryKeys(primaryKeys);

        TableData tableData = new TableData();
        if (list.isEmpty()) {
            tableData.setRealTotal(0);
        } else if (list.size() > reqVo.getPageSize()) {
            list = list.subList(0, reqVo.getPageSize());
            tableData.setTotal(reqVo.getPage() * reqVo.getPageSize() + 1);
        } else {
            tableData.setRealTotal((reqVo.getPage() - 1) * reqVo.getPageSize() + list.size());
        }
        tableData.setRecords(list);

        resVo.setTableData(tableData);

        if ((useNoneDefaultDb || SpringUtils.isProEnv()) && StringUtils.isNotBlank(tableName)) {
            List<Column> tableColumns = getTableColumns(jdbcTemplate, reqVo.getSelectDB(), tableName);
            Map<String, Column> map = Maps.uniqueIndex(tableColumns, Column::getName);
            resVo.setTableColumns(map);
        } else {
            resVo.setTableColumns(Collections.emptyMap());
        }
        SessionUtils.saveToSession(QUERY_RES, list);
        addSqlToExecuteHistory(tmpSql, jdbcTemplate, tokenVo, reqVo.getQueryId(), Collections.emptyList(), end - start);
        return resVo;
    }

    @Override
    protected String generateCsv(List<Map<String, Object>> records, String sql, PlainSelect plainSelect) {
        List<String> columnNames = getColumnNames(records, sql, plainSelect);
        String s = records.stream()
                .map(record -> record.values().stream()
                        .map(value -> {
                            if (value == null) {
                                return "(null)";
                            } else {
                                if (value instanceof Date) {
                                    return String.format("\"%s\"", value);
                                } else if (value instanceof String) {
                                    return String.format("\"%s\"", value);
                                } else {
                                    return value.toString();
                                }
                            }
                        })
                        .collect(Collectors.joining("\t")))
                .collect(Collectors.joining("\n"));
        return String.join("\t", columnNames) + "\n" + s;
    }

    @Override
    protected String generateUpdateSql(List<Map<String, Object>> records, String sql, PlainSelect plainSelect, List<Column> primaryColumns) {
        List<String> primaryNames = primaryColumns.stream().map(Column::getName).collect(Collectors.toList());
        Map<String, Object> primaryMap = Maps.newLinkedHashMap();
        for (String primaryName : primaryNames) {
            primaryMap.put(primaryName, null);
        }
        Set<String> columnNames = Sets.newHashSet(getColumnNames(records, sql, plainSelect));
        return records.stream()
                .map(record -> String.format("UPDATE %s SET %s WHERE %s;",
                                getTableName(plainSelect),
                                record.entrySet().stream()
                                        .map(entry -> {
                                            String key = entry.getKey();
                                            Object value = entry.getValue();
                                            if (primaryNames.contains(key)) {
                                                primaryMap.put(key, value);
                                                return null;
                                            }
                                            if (!columnNames.contains(key)) {
                                                return null;
                                            }
                                            return entryToCondition(entry);
                                        })
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.joining(", ")),
                                primaryMap.entrySet().stream()
                                        .map(this::entryToCondition)
                                        .collect(Collectors.joining(" AND "))
                        )
                )
                .collect(Collectors.joining("\r\n"));
    }

    @Override
    protected String generateInsertSql(List<Map<String, Object>> records, String sql, PlainSelect plainSelect) {
        List<String> columnNames = getColumnNames(records, sql, plainSelect);
        return records.stream()
                .map(record -> String.format("INSERT INTO %s (%s) VALUES (%s);",
                                getTableName(plainSelect),
                                String.join(", ", columnNames),
                                record.values().stream()
                                        .map(value -> {
                                            if (value == null) {
                                                return "null";
                                            } else {
                                                if (value instanceof Date) {
                                                    return String.format("'%s'", value);
                                                } else if (value instanceof String) {
                                                    return String.format("'%s'", value);
                                                } else {
                                                    return value.toString();
                                                }
                                            }
                                        })
                                        .collect(Collectors.joining(", "))
                        )
                )
                .collect(Collectors.joining("\r\n"));
    }

    @Override
    protected byte[] xlsBytes(List<Map<String, Object>> records, String tableName) {
        List<List<Object>> list = records.stream()
                .map(record -> {
                    modifyRowForSpecialColumnType(record);
                    record.forEach((key, value) -> {
                        if (value instanceof Date) {
                            record.replace(key, DateFormatUtils.format((Date) value, STANDARD_PATTERN));
                        }
                    });
                    return Lists.newArrayList(record.values());
                })
                .collect(Collectors.toList());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExcelWriter excelWriter = new ExcelWriterBuilder().registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .excelType(ExcelTypeEnum.XLS).autoCloseStream(true).file(outputStream).build();
        WriteSheet sheet = new WriteSheet();

        List<List<String>> head = records.get(0).keySet().stream().map(Lists::newArrayList).collect(Collectors.toList());
        sheet.setHead(head);
        sheet.setSheetName(tableName);

        excelWriter.write(list, sheet);
        excelWriter.finish();
        byte[] bytes = outputStream.toByteArray();
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    public Pair<String, Triple<Boolean, String, List<String>>> modifyQuerySql(PlainSelect plainSelect, String sql,
                                                                              JdbcTemplate jdbcTemplate, CommonErdVo reqVo) {
        int offset = (reqVo.getPage() - 1) * reqVo.getPageSize();
        boolean noLimit = !sql.toLowerCase().contains("limit");
        List<String> tablePrimaryKeys = Lists.newArrayList();
        String tableName = null;

        if (noLimit) {
            sql += " limit " + offset + "," + (reqVo.getPageSize() + 1);
        }

        if (plainSelect.getGroupBy() == null && CollectionUtils.isEmpty(plainSelect.getJoins())) {
            tableName = getTableName(plainSelect);
            tablePrimaryKeys = getPrimaryKeys(jdbcTemplate, reqVo.getSelectDB(), tableName);
        }

        return Pair.of(sql, Triple.of(noLimit, tableName, tablePrimaryKeys));
    }

    private void modifyRowForSpecialColumnType(Map<String, Object> rowMap) {
        for (Map.Entry<String, Object> rowEntry : rowMap.entrySet()) {
            Object columnValue = rowEntry.getValue();
            if (columnValue instanceof Long
                    || columnValue instanceof BigInteger
                    || columnValue instanceof BigDecimal) {
                columnValue = columnValue.toString();
            }
            rowMap.put(rowEntry.getKey(), columnValue);
        }
    }

    @Override
    public ModulesBean refreshModule(ErdOnlineModel erdOnlineModel, String moduleName) {
        DbsBean dbsBean = getDefaultDb(erdOnlineModel);
        ModulesBean modulesBean = ErdUtils.findModulesBean(moduleName, erdOnlineModel);
        List<DatatypeBean> datatypeBeans = erdOnlineModel.getProjectJSON().getDataTypeDomains().getDatatype();

        String urlDbName = DbUtils.resolveUrlDbName(dbsBean.getProperties().getUrl());

        JdbcTemplate jdbcTemplate = jdbcTemplateSingleton(dbsBean.getProperties(), urlDbName);

        return jdbcTemplate.execute((ConnectionCallback<ModulesBean>) con -> {
            DatabaseMetaData databaseMetaData = con.getMetaData();
            List<EntitiesBean> entitiesBeans = modulesBean.getEntities().stream()
                    .map(entitiesBean -> ErdUtils.tableToEntity(entitiesBean, databaseMetaData, datatypeBeans, jdbcTemplate))
                    .collect(Collectors.toList());
            modulesBean.setEntities(entitiesBeans);
            return modulesBean;
        });
    }

}
