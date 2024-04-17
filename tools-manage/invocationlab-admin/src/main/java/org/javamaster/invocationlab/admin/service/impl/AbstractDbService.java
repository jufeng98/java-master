package org.javamaster.invocationlab.admin.service.impl;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.consts.ErdConst;
import org.javamaster.invocationlab.admin.enums.SqlTypeEnum;
import org.javamaster.invocationlab.admin.model.erd.ApplyBean;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.CommonErdVo;
import org.javamaster.invocationlab.admin.model.erd.DatatypeBean;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.EntitiesBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.ExecuteHistoryBean;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.PropertiesBean;
import org.javamaster.invocationlab.admin.model.erd.SqlExecResVo;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.javamaster.invocationlab.admin.model.erd.TableData;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.serializer.ArrayListConverter;
import org.javamaster.invocationlab.admin.serializer.DocumentConverter;
import org.javamaster.invocationlab.admin.serializer.ObjectIdConverter;
import org.javamaster.invocationlab.admin.service.DbService;
import org.javamaster.invocationlab.admin.util.DbUtils;
import org.javamaster.invocationlab.admin.util.ErdUtils;
import org.javamaster.invocationlab.admin.util.ExecutorUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.javamaster.invocationlab.admin.util.SessionUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX;
import static org.javamaster.invocationlab.admin.consts.ErdConst.QUERY_RES;
import static org.javamaster.invocationlab.admin.util.DbUtils.getDefaultDb;
import static org.javamaster.invocationlab.admin.util.DbUtils.jdbcTemplateSingleton;
import static org.javamaster.invocationlab.admin.util.JsonUtils.STANDARD_PATTERN;

@Slf4j
public abstract class AbstractDbService implements DbService {
    private static final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    protected String getCheckSql() {
        return "select now()";
    }

    protected String getExplainSql(String querySql) {
        return "explain " + querySql;
    }

    @Override
    public void checkDb(DbsBean dbsBean) {
        try {
            PropertiesBean propertiesBean = dbsBean.getProperties();
            String urlDbName = DbUtils.resolveUrlDbName(propertiesBean.getUrl());

            JdbcTemplate jdbcTemplate = jdbcTemplateSingleton(propertiesBean, urlDbName);
            jdbcTemplate.execute(getCheckSql());
        } catch (Exception e) {
            log.error("checkDb error", e);
            throw new ErdException(e.getMessage());
        }
    }

    @Override
    public List<String> getDbNames(DbsBean dbsBean) {
        PropertiesBean propertiesBean = dbsBean.getProperties();
        String urlDbName = DbUtils.resolveUrlDbName(propertiesBean.getUrl());

        JdbcTemplate jdbcTemplate = jdbcTemplateSingleton(propertiesBean, urlDbName);
        return getDbNames(jdbcTemplate);
    }

    public abstract List<String> getDbNames(JdbcTemplate jdbcTemplate);

    public String tryGetTableNameIfSingleTableQuery(String sql) {
        SQLSelectStatement sqlStatement = (SQLSelectStatement) SQLUtils.parseSingleStatement(sql, getDbType());
        SQLSelectQuery sqlSelectQuery = sqlStatement.getSelect().getQuery();

        return tryGetTableNameIfSingleTableQuery(sqlSelectQuery);
    }

    /**
     * 如果是单表简单查询,则返回表名,否则返回空
     */
    public String tryGetTableNameIfSingleTableQuery(SQLSelectQuery sqlSelectQuery) {
        if (!(sqlSelectQuery instanceof SQLSelectQueryBlock)) {
            return "";
        }

        SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
        SQLTableSource sqlTableSource = sqlSelectQueryBlock.getFrom();
        if (!(sqlTableSource instanceof SQLExprTableSource)) {
            return "";
        }

        SQLSelectGroupByClause groupBy = sqlSelectQueryBlock.getGroupBy();
        if (groupBy != null) {
            return "";
        }

        boolean hasMethod = sqlSelectQueryBlock.getSelectList().stream()
                .anyMatch(it -> it.getExpr() instanceof SQLMethodInvokeExpr);
        if (hasMethod) {
            return "";
        }

        SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
        return sqlExprTableSource.getTableName();
    }

    @Override
    public List<Table> getTables(DbsBean dbsBean, String selectDB) {
        JdbcTemplate jdbcTemplate = jdbcTemplateSingleton(dbsBean.getProperties(), selectDB);
        return getTables(jdbcTemplate);
    }

    public List<Table> getTables(JdbcTemplate jdbcTemplate) {
        return DbUtils.getTables(jdbcTemplate);
    }


    protected Object convertToSuitableObj(Column column, String val) {
        TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(JdbcType.forCode(column.getType()));
        Class<?> typeHandlerClass = typeHandler.getClass();

        ParameterizedType genericInterfaces = (ParameterizedType) typeHandlerClass.getGenericSuperclass();
        Class<?> actualType = (Class<?>) genericInterfaces.getActualTypeArguments()[0];

        ConversionService conversionService = SpringUtils.getContext().getBean(ConversionService.class);
        return conversionService.convert(val, actualType);
    }

    protected void modifyQueryRow(Map<String, Object> rowMap) {
    }

    private static void modifyRowForSpecialColumnType(Map<String, Object> rowMap) {
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

    /**
     * 去掉值为null的列
     */
    public static JSONObject removeNullColumns(LinkedHashMap<String, Object> reqRow) {
        List<String> keys = reqRow.entrySet().stream()
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        keys.forEach(reqRow::remove);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(reqRow);
        return jsonObject;
    }

    @Override
    public List<SqlExecResVo> execUpdate(DbsBean dbsBean, TokenVo tokenVo, CommonErdVo reqVo) {
        JdbcTemplate jdbcTemplate = jdbcTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());
        TransactionTemplate transactionTemplate = DbUtils.transactionTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());
        return execUpdate(jdbcTemplate, transactionTemplate, tokenVo, reqVo);
    }

    protected List<SqlExecResVo> execUpdate(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate,
                                            TokenVo tokenVo, CommonErdVo reqVo) {
        String tableName = tryGetTableNameIfSingleTableQuery(reqVo.getSql());
        List<Column> tableColumns = getTableColumns(jdbcTemplate, tableName);
        ImmutableMap<String, Column> columnMap = Maps.uniqueIndex(tableColumns, Column::getName);
        List<String> primaryKeyNames = filterPrimaryKeys(tableColumns);

        return transactionTemplate.execute(transactionStatus -> reqVo.getRows().stream()
                .map(reqRow -> {
                    //noinspection unchecked
                    JSONObject row = removeNullColumns((LinkedHashMap<String, Object>) reqRow);

                    Pair<String, List<Pair<Object, JdbcType>>> pairPrimary = primaryKeyConditions(row, columnMap, primaryKeyNames);

                    Pair<String, List<Pair<Object, JdbcType>>> tmpPair;
                    SqlTypeEnum sqlTypeEnum = SqlTypeEnum.getByType(row.getString(ErdConst.ROW_OPERATION_TYPE));
                    if (sqlTypeEnum == SqlTypeEnum.DELETE) {
                        tmpPair = handleDeleteOperation(columnMap, pairPrimary, row, tableName);
                    } else if (sqlTypeEnum == SqlTypeEnum.UPDATE) {
                        tmpPair = handleEditOperation(columnMap, pairPrimary, row, tableName);
                    } else if (sqlTypeEnum == SqlTypeEnum.INSERT) {
                        tmpPair = handleInsertOperation(columnMap, row, tableName);
                    } else {
                        throw new IllegalArgumentException(sqlTypeEnum.type);
                    }

                    String actualSql = tmpPair.getLeft();
                    List<Pair<Object, JdbcType>> fieldValues = tmpPair.getRight();

                    int index = row.getIntValue(ErdConst.INDEX);
                    return executeUpdateSql(jdbcTemplate, actualSql, tokenVo, reqVo.getQueryId(), fieldValues, index);
                })
                .collect(Collectors.toList()));
    }

    protected List<String> getPrimaryKeys(JdbcTemplate jdbcTemplate, String tableName) {
        List<Column> tableColumns = getPrimaryColumns(jdbcTemplate, tableName);
        return tableColumns.stream()
                .map(Column::getName)
                .collect(Collectors.toList());
    }

    protected List<Column> getPrimaryColumns(JdbcTemplate jdbcTemplate, String tableName) {
        List<Column> tableColumns = getTableColumns(jdbcTemplate, tableName);
        return tableColumns.stream()
                .filter(Column::isPrimaryKey)
                .collect(Collectors.toList());
    }

    protected List<String> filterPrimaryKeys(List<Column> tableColumns) {
        return tableColumns.stream()
                .filter(Column::isPrimaryKey)
                .map(Column::getName)
                .collect(Collectors.toList());
    }

    private List<Pair<Object, JdbcType>> convertRowToRealFields(Map<String, Object> oldRow, ImmutableMap<String, Column> columnMap) {
        return oldRow.entrySet().stream()
                .map(entry -> {
                    Column column = columnMap.get(entry.getKey());
                    Object val = convertToSuitableObj(column, (String) entry.getValue());
                    return Pair.of(val, JdbcType.forCode(column.getType()));
                })
                .collect(Collectors.toList());
    }

    protected Pair<String, List<Pair<Object, JdbcType>>> handleDeleteOperation(ImmutableMap<String, Column> columnMap,
                                                                               Pair<String, List<Pair<Object, JdbcType>>> pairPrimary,
                                                                               JSONObject row,
                                                                               String tableName) {
        String sql;
        List<Pair<Object, JdbcType>> fieldValues = Lists.newArrayList();
        List<Pair<Object, JdbcType>> primaryValues = pairPrimary.getRight();

        boolean noPrimaryKeyInRequest = primaryValues.stream().anyMatch(Objects::isNull);
        if (noPrimaryKeyInRequest) {
            Map<String, Object> oldRow = getOldQueryMap(row.get(ErdConst.ERD_ROW_KEY));
            oldRow.remove(ErdConst.ERD_ROW_KEY);

            String conditions = oldRow.keySet().stream()
                    .map(key -> key + "=?")
                    .collect(Collectors.joining(" AND "));
            sql = String.format("delete from %s where %s", tableName, conditions);

            List<Pair<Object, JdbcType>> pairs = convertRowToRealFields(oldRow, columnMap);
            fieldValues.addAll(pairs);
        } else {
            sql = String.format("delete from %s where %s", tableName, pairPrimary.getLeft());
            fieldValues.addAll(primaryValues);
        }
        return Pair.of(sql, fieldValues);
    }

    protected Pair<String, List<Pair<Object, JdbcType>>> handleEditOperation(ImmutableMap<String, Column> columnMap,
                                                                             Pair<String, List<Pair<Object, JdbcType>>> pairPrimary,
                                                                             JSONObject row, String tableName) {
        Map<String, Object> oldRow = getOldQueryMap(row.get(ErdConst.ERD_ROW_KEY));
        if (oldRow == null) {
            throw new ErdException("请重新刷新查询后再重试");
        }

        String sql;
        List<Pair<Object, JdbcType>> fieldValues = Lists.newArrayList();

        String fields = row.entrySet().stream()
                .filter(this::filterUnRelateColumn)
                .filter(entry -> filterUnChangeColumn(entry, oldRow))
                .map(entry -> {
                    String columnName = entry.getKey();
                    String columnValue = String.valueOf(entry.getValue());

                    Column column = columnMap.get(columnName);
                    JdbcType jdbcType = JdbcType.forCode(column.getType());

                    if (ErdConst.NULL_VALUE.equals(columnValue)) {
                        fieldValues.add(Pair.of(null, jdbcType));
                    } else {
                        Object val = convertToSuitableObj(column, columnValue);
                        fieldValues.add(Pair.of(val, JdbcType.forCode(column.getType())));
                    }
                    return columnName + "=?";
                })
                .collect(Collectors.joining(","));
        if (StringUtils.isBlank(fields)) {
            throw new ErdException("数据无变化,未做任何更新操作!");
        }

        List<Pair<Object, JdbcType>> primaryValues = pairPrimary.getRight();
        boolean noPrimaryKeyInRequest = primaryValues.stream().anyMatch(Objects::isNull);
        if (noPrimaryKeyInRequest) {
            oldRow.remove(ErdConst.ERD_ROW_KEY);

            String conditions = oldRow.keySet().stream()
                    .map(key -> key + "=?")
                    .collect(Collectors.joining(" AND "));
            sql = String.format("update %s set %s where %s", tableName, fields, conditions);

            List<Pair<Object, JdbcType>> pairs = convertRowToRealFields(oldRow, columnMap);
            fieldValues.addAll(pairs);
        } else {
            sql = String.format("update %s set %s where %s", tableName, fields, pairPrimary.getLeft());
            fieldValues.addAll(primaryValues);
        }
        return Pair.of(sql, fieldValues);
    }

    protected Pair<String, List<Pair<Object, JdbcType>>> handleInsertOperation(ImmutableMap<String, Column> columnMap,
                                                                               JSONObject row,
                                                                               String tableName) {
        List<Pair<Object, JdbcType>> fieldValues = Lists.newArrayList();
        List<String> names = Lists.newArrayList();

        row.entrySet().stream()
                .filter(this::filterUnRelateColumn)
                .forEach(entry -> {
                    String columnName = entry.getKey();
                    String columnValue = String.valueOf(entry.getValue());

                    Column column = columnMap.get(columnName);
                    JdbcType jdbcType = JdbcType.forCode(column.getType());

                    names.add(columnName);

                    if (ErdConst.NULL_VALUE.equals(columnValue)) {
                        fieldValues.add(Pair.of(null, jdbcType));
                    } else {
                        Object val = convertToSuitableObj(column, columnValue);
                        fieldValues.add(Pair.of(val, jdbcType));
                    }
                });
        String fields = String.join(",", names);

        String values = names.stream()
                .map(name -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format("insert into %s (%s) values (%s)", tableName, fields, values);
        return Pair.of(sql, fieldValues);
    }

    protected SqlExecResVo executeUpdateSql(JdbcTemplate jdbcTemplate, String sql, TokenVo tokenVo,
                                            String queryId, List<Pair<Object, JdbcType>> fieldValues, int index) {
        log.info("{}-{} execute dml sql:\n{},\nparams:{}", tokenVo.getUserId(), tokenVo.getUsername(), sql, fieldValues);
        long start = System.currentTimeMillis();

        @SuppressWarnings({"DataFlowIssue", "ConstantConditions"})
        int num = jdbcTemplate.execute(sql, (PreparedStatementCallback<Integer>) ps -> {
            for (int i = 0; i < fieldValues.size(); i++) {
                Pair<Object, JdbcType> valuePair = fieldValues.get(i);
                Object fieldValue = valuePair.getLeft();
                JdbcType fieldType = valuePair.getRight();
                @SuppressWarnings("unchecked")
                TypeHandler<Object> typeHandler = (TypeHandler<Object>) typeHandlerRegistry.getTypeHandler(fieldType);
                typeHandler.setParameter(ps, i + 1, fieldValue, fieldType);
            }
            return ps.executeUpdate();
        });

        long end = System.currentTimeMillis();
        log.info("{}-{} execute dml sql res num:{}", tokenVo.getUserId(), tokenVo.getUsername(), num);

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setColumns(Sets.newHashSet("affect_num"));
        resVo.setTableColumns(Collections.emptyMap());
        resVo.setPrimaryKeys(Collections.emptyList());
        resVo.setQueryKey(ErdConst.COUNTER.incrementAndGet());

        TableData tableData = new TableData();
        tableData.setTotal(1);

        Map<String, Object> map = Maps.newHashMap();
        map.put("affect_num", num);
        //noinspection unchecked
        tableData.setRecords(Lists.newArrayList(map));

        resVo.setTableData(tableData);

        if (num != 1) {
            addSqlToExecuteHistory(sql + " (已回滚)", jdbcTemplate, tokenVo, queryId, fieldValues, end - start);
            throw new ErdException("第 " + (index + 1) + " 行记录根据结果集条件不能确定唯一的记录,期望影响 1 条,实际会影响 " + num + " 条!");
        } else {
            addSqlToExecuteHistory(sql, jdbcTemplate, tokenVo, queryId, fieldValues, end - start);
        }
        return resVo;
    }

    @Override
    public List<Column> getTableColumns(DbsBean dbsBean, String selectDB, String tableName) {
        JdbcTemplate jdbcTemplate = jdbcTemplateSingleton(dbsBean.getProperties(), selectDB);
        return getTableColumns(jdbcTemplate, tableName);
    }

    protected List<Column> getTableColumns(JdbcTemplate jdbcTemplate, String tableName) {
        return DbUtils.getTableColumns(jdbcTemplate, tableName);
    }

    protected boolean isDmlSql(String lowerSql) {
        return lowerSql.startsWith(SqlTypeEnum.UPDATE.type)
                || lowerSql.startsWith(SqlTypeEnum.DELETE.type)
                || lowerSql.startsWith(SqlTypeEnum.INSERT.type);
    }

    protected boolean isDdlSql(String lowerSql) {
        return lowerSql.startsWith(SqlTypeEnum.CREATE.type)
                || lowerSql.startsWith(SqlTypeEnum.ALTER.type)
                || lowerSql.startsWith(SqlTypeEnum.DROP.type);
    }

    @Override
    public SqlExecResVo execSql(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo) {
        boolean useNoneDefaultDb = !dbsBean.getName().equals(reqVo.getSelectDB());

        JdbcTemplate jdbcTemplate = jdbcTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());
        if (reqVo.getExplain()) {
            return executeExplainSql(reqVo, jdbcTemplate, tokenVo);
        }

        String lowerSql = reqVo.getSql().toLowerCase();
        boolean ddlSql = isDdlSql(lowerSql);
        boolean dmlSql = isDmlSql(lowerSql);
        if (dmlSql || ddlSql) {
            TransactionTemplate transactionTemplate = DbUtils.transactionTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());
            return transactionTemplate.execute(status -> executeDmlOrDdlSql(jdbcTemplate, tokenVo, ddlSql, reqVo));
        }

        return executeDqlSql(jdbcTemplate, tokenVo, useNoneDefaultDb, reqVo);
    }

    protected SqlExecResVo executeExplainSql(CommonErdVo reqVo, JdbcTemplate jdbcTemplate, TokenVo tokenVo) {
        String explainSql = getExplainSql(reqVo.getSql());
        log.info("{}-{} execute explain sql:\n{}", tokenVo.getUserId(), tokenVo.getUsername(), explainSql);

        long start = System.currentTimeMillis();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(explainSql);
        long end = System.currentTimeMillis();

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setColumns(list.get(0).keySet());

        TableData tableData = new TableData();
        tableData.setTotal(1);
        tableData.setRecords(list);
        resVo.setTableData(tableData);

        addSqlToExecuteHistory(explainSql, jdbcTemplate, tokenVo, reqVo.getQueryId(), Collections.emptyList(), end - start);
        return resVo;
    }

    protected SqlExecResVo executeDmlOrDdlSql(JdbcTemplate jdbcTemplate, TokenVo tokenVo, boolean ddlSql, CommonErdVo reqVo) {
        List<String> sqlList = Arrays.stream(reqVo.getSql().split(";"))
                .collect(Collectors.toList());
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
        resVo.setQueryKey(ErdConst.COUNTER.incrementAndGet());

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

    protected SqlExecResVo executeDqlSql(JdbcTemplate jdbcTemplate, TokenVo tokenVo, boolean useNoneDefaultDb, CommonErdVo reqVo) {
        Pair<String, Triple<Boolean, String, List<String>>> pair = modifyQuerySql(jdbcTemplate, reqVo);

        String sql = pair.getLeft();
        boolean showPagination = pair.getRight().getLeft();
        List<String> primaryKeys = pair.getRight().getRight();

        log.info("{}-{} execute dql sql:\n{}", tokenVo.getUserId(), tokenVo.getUsername(), sql);
        long start = System.currentTimeMillis();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        long end = System.currentTimeMillis();
        log.info("{}-{} execute dql sql res size:{}", tokenVo.getUserId(), tokenVo.getUsername(), list.size());

        Set<String> columns = Sets.newLinkedHashSet();
        String tableName = pair.getRight().getMiddle();
        if (list.isEmpty()) {
            List<Column> tableColumns = getTableColumns(jdbcTemplate, tableName);
            List<String> columnNames = tableColumns.stream()
                    .map(Column::getName)
                    .collect(Collectors.toList());

            columns.addAll(columnNames);
            columns.add(ErdConst.ERD_ROW_KEY);
        } else {
            columns.addAll(list.get(0).keySet());
            columns.add(ErdConst.ERD_ROW_KEY);

            int anInt = Integer.parseInt(RandomUtils.nextInt(1000000, 9999999) + "01");
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> rowMap = list.get(i);
                rowMap.put(ErdConst.ERD_ROW_KEY, anInt + i);

                modifyQueryRow(rowMap);

                if (!reqVo.getIsExport()) {
                    modifyRowForSpecialColumnType(rowMap);
                }
            }
        }

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setColumns(columns);
        resVo.setQueryKey(ErdConst.COUNTER.incrementAndGet());
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
            List<Column> tableColumns = getTableColumns(jdbcTemplate, tableName);
            Map<String, Column> columnMap = Maps.uniqueIndex(tableColumns, Column::getName);
            resVo.setTableColumns(columnMap);
        } else {
            resVo.setTableColumns(Collections.emptyMap());
        }

        SessionUtils.saveToSession(QUERY_RES, list);
        addSqlToExecuteHistory(sql, jdbcTemplate, tokenVo, reqVo.getQueryId(), Collections.emptyList(), end - start);
        return resVo;
    }

    protected Map<String, Object> getOldQueryMap(Object rowUniqueKey) {
        List<Map<String, Object>> oldRecords = SessionUtils.getFromSession(QUERY_RES);
        if (oldRecords == null) {
            throw new ErdException("请重新刷新查询后再重试");
        }
        List<Map<String, Object>> list = oldRecords.stream()
                .filter(oldRecord -> oldRecord.get(ErdConst.ERD_ROW_KEY).equals(rowUniqueKey))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        Map<String, Object> oldRow = list.get(0);
        return Maps.newLinkedHashMap(oldRow);
    }

    /**
     * 尝试根据主键信息取得主键值
     *
     * @param row               入参行
     * @param columnMap         列元数据
     * @param primaryKeyColumns 主键名列表
     * @return 返回主键信息, 格式如 Pair.of(<"id = ? and secondId = ?", [1, 2]>)
     */
    protected Pair<String, List<Pair<Object, JdbcType>>> primaryKeyConditions(JSONObject row,
                                                                              ImmutableMap<String, Column> columnMap,
                                                                              List<String> primaryKeyColumns) {
        Map<String, Object> oldRow = getOldQueryMap(row.get(ErdConst.ERD_ROW_KEY));
        if (oldRow == null) {
            return null;
        }

        List<Pair<Object, JdbcType>> primaryKeyValues = primaryKeyColumns.stream()
                .map(key -> {
                    Object val = oldRow.get(key);
                    Column column = columnMap.get(key);

                    JdbcType jdbcType = JdbcType.forCode(column.getType());
                    Object obj = convertToSuitableObj(column, (String) val);

                    return Pair.of(obj, jdbcType);
                })
                .collect(Collectors.toList());

        String key = primaryKeyColumns.stream()
                .map(primaryKey -> primaryKey + "=?")
                .collect(Collectors.joining(" and "));

        return Pair.of(key, primaryKeyValues);
    }

    protected boolean filterUnRelateColumn(Map.Entry<String, Object> entry) {
        return !ErdConst.ROW_OPERATION_TYPE.equals(entry.getKey())
                && !ErdConst.ERD_ROW_KEY.equals(entry.getKey())
                && !ErdConst.INDEX.equals(entry.getKey());
    }

    protected boolean filterUnChangeColumn(Map.Entry<String, Object> rowEntry, Map<String, Object> oldRowMap) {
        String columnName = rowEntry.getKey();
        Object columnValue = rowEntry.getValue();
        Object dbColumnValue = oldRowMap.get(columnName);
        if (dbColumnValue instanceof Date) {
            if (StringUtils.isEmpty(columnValue.toString())) {
                return true;
            }
            return !dbColumnValue.toString().contains(columnValue.toString());
        } else if (dbColumnValue instanceof Long
                || dbColumnValue instanceof BigInteger
                || dbColumnValue instanceof BigDecimal) {
            if (StringUtils.isEmpty(columnValue.toString())) {
                return true;
            }
            return !columnValue.equals(dbColumnValue.toString());
        }
        // 过滤掉值未发生变化的列
        return !columnValue.equals(dbColumnValue);
    }

    @Override
    public Triple<String, MediaType, byte[]> exportSql(DbsBean dbsBean, TokenVo tokenVo, CommonErdVo reqVo) {
        JdbcTemplate jdbcTemplate = jdbcTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());
        return exportSql(jdbcTemplate, dbsBean, tokenVo, reqVo);
    }

    protected Triple<String, MediaType, byte[]> exportSql(JdbcTemplate jdbcTemplate, DbsBean dbsBean,
                                                          TokenVo tokenVo, CommonErdVo reqVo) {
        List<Map<String, Object>> oldRecords = SessionUtils.getFromSession(QUERY_RES);

        SqlExecResVo resVo = execSql(reqVo, dbsBean, tokenVo);

        SessionUtils.saveToSession(QUERY_RES, oldRecords);

        List<Map<String, Object>> records = resVo.getTableData().getRecords();
        records.forEach(record -> {
            record.remove(ErdConst.ERD_ROW_KEY);
            record.remove(ErdConst.INDEX);
        });

        String type = reqVo.getType();
        String sql = reqVo.getSql();
        String fileName = "SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + "." + reqVo.getType();

        if ("json".equals(type)) {
            String jsonStr = JsonUtils.objectToString(records);
            return Triple.of(fileName, MediaType.TEXT_PLAIN, jsonStr.getBytes(StandardCharsets.UTF_8));
        }

        if ("csv".equals(type)) {
            String res = generateCsv(records);
            return Triple.of(fileName, MediaType.TEXT_PLAIN, res.getBytes(StandardCharsets.UTF_8));
        }

        SQLSelectStatement sqlStatement = (SQLSelectStatement) SQLUtils.parseSingleStatement(sql, getDbType().name());
        SQLSelectQuery sqlSelectQuery = sqlStatement.getSelect().getQuery();

        String tableName = tryGetTableNameIfSingleTableQuery(sqlSelectQuery);

        if ("xls".equals(type)) {
            byte[] bytes = excelBytes(records, tableName);
            return Triple.of(fileName, new MediaType("application", "vnd.ms-excel"), bytes);
        }

        if (StringUtils.isBlank(tableName)) {
            throw new ErdException("不支持的操作");
        }

        if ("sqlInsert".equals(type)) {
            fileName = "INSERT SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + ".sql";
            String tmpSql = generateInsertSql(records, sqlSelectQuery);
            return Triple.of(fileName, MediaType.TEXT_PLAIN, tmpSql.getBytes(StandardCharsets.UTF_8));
        }

        if ("sqlUpdate".equals(type)) {
            fileName = "UPDATE SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + ".sql";
            List<Column> primaryColumns = getPrimaryColumns(jdbcTemplate, tableName);
            String tmpSql = generateUpdateSql(records, sqlSelectQuery, primaryColumns);
            return Triple.of(fileName, MediaType.TEXT_PLAIN, tmpSql.getBytes(StandardCharsets.UTF_8));
        }

        throw new IllegalArgumentException(type);
    }

    public static String generateCsv(List<Map<String, Object>> records) {
        List<String> columnNames = new ArrayList<>(records.get(0).keySet());

        String s = records.stream()
                .map(record -> record.values().stream()
                        .map(value -> {
                            if (value == null) {
                                return "(null)";
                            }

                            if (value instanceof String) {
                                return String.format("\"%s\"", ((String) value).replace("\"", "\"\""));
                            } else if (value instanceof Document) {
                                return String.format("\"%s\"", ((Document) value).toJson().replace("\"", "\"\""));
                            } else if (value instanceof ObjectId) {
                                return String.format("\"%s\"", ((ObjectId) value).toHexString());
                            } else if (value instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<Document> list = (List<Document>) value;
                                String str = list.stream()
                                        .map(it -> it.toJson().replace("\"", "\"\""))
                                        .collect(Collectors.joining(",", "[", "]"));
                                return String.format("\"%s\"", str);
                            } else {
                                return value.toString().replace("\"", "\"\"");
                            }
                        })
                        .collect(Collectors.joining("\t")))
                .collect(Collectors.joining("\n"));
        return String.join("\t", columnNames) + "\n" + s;
    }

    protected String generateUpdateSql(List<Map<String, Object>> records, SQLSelectQuery sqlSelectQuery,
                                       List<Column> primaryColumns) {
        String tableName = tryGetTableNameIfSingleTableQuery(sqlSelectQuery);
        List<String> columnNameList = getColumnNames(records, sqlSelectQuery);

        List<String> primaryNames = primaryColumns.stream()
                .map(Column::getName)
                .collect(Collectors.toList());

        Map<String, Object> primaryMap = Maps.newLinkedHashMap();
        for (String primaryName : primaryNames) {
            primaryMap.put(primaryName, null);
        }

        Set<String> columnNames = Sets.newHashSet(columnNameList);
        return records.stream()
                .map(record -> {
                            String fields = record.entrySet().stream()
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
                                    .collect(Collectors.joining(", "));

                            String conditions = primaryMap.entrySet().stream()
                                    .map(this::entryToCondition)
                                    .collect(Collectors.joining(" AND "));

                            return String.format("UPDATE %s SET %s WHERE %s;", tableName, fields, conditions);
                        }
                )
                .collect(Collectors.joining("\r\n"));
    }

    protected String generateInsertSql(List<Map<String, Object>> records, SQLSelectQuery sqlSelectQuery) {
        String tableName = tryGetTableNameIfSingleTableQuery(sqlSelectQuery);
        List<String> columnNames = getColumnNames(records, sqlSelectQuery);

        return records.stream()
                .map(record -> {
                            String fields = String.join(", ", columnNames);

                            String values = record.values().stream()
                                    .map(value -> {
                                        if (value == null) {
                                            return "null";
                                        }

                                        value = convertColumnValue(value);

                                        if (value instanceof Date) {
                                            return String.format("'%s'", value);
                                        } else if (value instanceof String) {
                                            return String.format("'%s'", value);
                                        } else {
                                            return value.toString();
                                        }
                                    })
                                    .collect(Collectors.joining(", "));

                            return String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, fields, values);
                        }
                )
                .collect(Collectors.joining("\r\n"));
    }

    private static Object convertColumnValue(Object columnValue) {
        if (columnValue instanceof Long
                || columnValue instanceof BigInteger
                || columnValue instanceof BigDecimal) {
            columnValue = columnValue.toString();
        } else if (columnValue instanceof Date) {
            columnValue = DateFormatUtils.format((Date) columnValue, STANDARD_PATTERN);
        }
        return columnValue;
    }

    @SneakyThrows
    protected static byte[] excelBytes(List<Map<String, Object>> records, String tableName) {
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

        @Cleanup
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExcelWriter excelWriter = new ExcelWriterBuilder()
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerConverter(ObjectIdConverter.INSTANCE)
                .registerConverter(ArrayListConverter.INSTANCE)
                .registerConverter(DocumentConverter.INSTANCE)
                .excelType(ExcelTypeEnum.XLS)
                .autoCloseStream(true)
                .file(outputStream)
                .build();

        List<List<String>> head = records.get(0).keySet().stream()
                .map(Lists::newArrayList)
                .collect(Collectors.toList());

        WriteSheet sheet = new WriteSheet();
        sheet.setHead(head);
        sheet.setSheetName(StringUtils.isNotBlank(tableName) ? tableName : "记录");

        excelWriter.write(list, sheet);
        excelWriter.finish();
        return outputStream.toByteArray();
    }

    @Override
    public ModulesBean refreshModule(ErdOnlineModel erdOnlineModel, String moduleName) {
        DbsBean dbsBean = getDefaultDb(erdOnlineModel);
        ModulesBean modulesBean = ErdUtils.findModulesBean(moduleName, erdOnlineModel);
        List<DatatypeBean> datatypeBeans = erdOnlineModel.getProjectJSON().getDataTypeDomains().getDatatype();

        String urlDbName = DbUtils.resolveUrlDbName(dbsBean.getProperties().getUrl());
        JdbcTemplate jdbcTemplate = jdbcTemplateSingleton(dbsBean.getProperties(), urlDbName);

        return refreshModule(jdbcTemplate, modulesBean, datatypeBeans);
    }

    public ModulesBean refreshModule(JdbcTemplate jdbcTemplate, ModulesBean modulesBean, List<DatatypeBean> datatypeBeans) {
        return jdbcTemplate.execute((ConnectionCallback<ModulesBean>) con -> {
            DatabaseMetaData databaseMetaData = con.getMetaData();
            List<EntitiesBean> entitiesBeans = modulesBean.getEntities().stream()
                    .map(entitiesBean -> {
                        Predicate<Pair<ApplyBean, Column>> predicate = datatypePredicate();
                        EntitiesBean bean = ErdUtils.tableToEntity(entitiesBean, databaseMetaData, datatypeBeans, predicate);

                        String tableName = entitiesBean.getTitle();
                        String ddl = getTableDdlSql(jdbcTemplate, tableName);

                        bean.setOriginalCreateTableSql(ddl);
                        return bean;
                    })
                    .collect(Collectors.toList());
            modulesBean.setEntities(entitiesBeans);

            return modulesBean;
        });
    }

    protected abstract Predicate<Pair<ApplyBean, Column>> datatypePredicate();

    protected abstract String getTableDdlSql(JdbcTemplate jdbcTemplate, String tableName);

    /**
     * 修改查询sql
     *
     * @return Pair<修改后的SQL, Triple < 是否缺少limit, 表名, 表主键列表>>
     */
    protected Pair<String, Triple<Boolean, String, List<String>>> modifyQuerySql(JdbcTemplate jdbcTemplate,
                                                                                 CommonErdVo reqVo) {
        String sql = reqVo.getSql();
        SQLStatement sqlStatement = SQLUtils.parseSingleStatement(sql, getDbType());
        if (!(sqlStatement instanceof SQLSelectStatement)) {
            return Pair.of(sql, Triple.of(false, null, Lists.newArrayList()));
        }

        SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
        SQLSelectQuery sqlSelectQuery = sqlSelectStatement.getSelect().getQuery();

        boolean noLimit = false;
        List<String> tablePrimaryKeys = Lists.newArrayList();
        String tableName = null;

        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
            if (sqlSelectQueryBlock.getLimit() == null) {
                noLimit = true;
                int offset = (reqVo.getPage() - 1) * reqVo.getPageSize();
                sqlSelectQueryBlock.limit(reqVo.getPageSize() + 1, offset);
            }

            tableName = tryGetTableNameIfSingleTableQuery(sqlSelectQuery);
            if (StringUtils.isNotBlank(tableName)) {
                tablePrimaryKeys = getPrimaryKeys(jdbcTemplate, tableName);
            }
        } else {
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            if (sqlUnionQuery.getLimit() == null) {
                noLimit = true;
                sqlUnionQuery.setLimit(new SQLLimit(reqVo.getPageSize()));
            }
        }
        sql = SQLUtils.toSQLString(sqlSelectStatement, getDbType());

        return Pair.of(sql, Triple.of(noLimit, tableName, tablePrimaryKeys));
    }

    protected List<String> getColumnNames(List<Map<String, Object>> records, SQLSelectQuery sqlSelectQuery) {
        SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
        List<SQLSelectItem> selectList = sqlSelectQueryBlock.getSelectList();

        List<String> columnNames;
        if (selectList.size() == 1 && selectList.get(0).getExpr().toString().contains("*")) {
            columnNames = new ArrayList<>(records.get(0).keySet());
        } else {
            columnNames = sqlSelectQueryBlock.getSelectList().stream()
                    .map(item -> {
                        SQLIdentifierExpr expr = (SQLIdentifierExpr) item.getExpr();
                        return expr.getName();
                    })
                    .collect(Collectors.toList());
        }
        return columnNames;
    }

    protected String entryToCondition(Map.Entry<String, Object> entry) {
        String key = entry.getKey();
        Object value = entry.getValue();
        if (value == null) {
            return key + "=null";
        } else {
            if (value instanceof Date) {
                return key + "=" + String.format("'%s'", value);
            } else if (value instanceof String) {
                return key + "=" + String.format("'%s'", value);
            } else {
                return key + "=" + value;
            }
        }
    }

    @Override
    public Integer getTableRecordTotal(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo) {
        long start = System.currentTimeMillis();

        JdbcTemplate jdbcTemplate = jdbcTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());

        String sql = reqVo.getSql().replace(";", "");

        String countSql;
        Integer count;

        String tableName = tryGetTableNameIfSingleTableQuery(sql);
        if (StringUtils.isNotBlank(tableName)) {
            int fromIndex = sql.toLowerCase().indexOf("from");
            countSql = "select count(*) " + sql.substring(fromIndex);
        } else {
            countSql = "select count(*) from (" + sql + ") tmp";
        }

        log.info("{}-{} execute count sql:\n{}", tokenVo.getUserId(), tokenVo.getUsername(), countSql);
        count = jdbcTemplate.queryForObject(countSql, Integer.class);

        addSqlToExecuteHistory(countSql, jdbcTemplate, tokenVo, reqVo.getQueryId(), Collections.emptyList(), System.currentTimeMillis() - start);
        return count;
    }

    protected void addSqlToExecuteHistory(String sql, JdbcTemplate jdbcTemplate, TokenVo tokenVo, String queryId,
                                          List<Pair<Object, JdbcType>> sqlValues, long duration) {
        ExecutorUtils.startAsyncTask(() -> {
            HikariDataSource dataSource = (HikariDataSource) jdbcTemplate.getDataSource();
            @SuppressWarnings("DataFlowIssue")
            String urlDbName = DbUtils.resolveUrlDbName(dataSource.getJdbcUrl());
            String key = ERD_PREFIX + "sqlHistory:" + queryId;
            ExecuteHistoryBean bean = new ExecuteHistoryBean();
            bean.setSqlInfo(sql);
            bean.setDbName(urlDbName);
            bean.setDuration(duration);
            bean.setCreateTime(new Date());
            bean.setCreator(tokenVo.getUserId());
            bean.setParams(sqlValues.toString());
            @SuppressWarnings("unchecked")
            RedisTemplate<String, Object> redisTemplateJackson = (RedisTemplate<String, Object>) SpringUtils.getContext()
                    .getBean("redisTemplateJackson");
            redisTemplateJackson.opsForList().leftPush(key, bean);
            Long size = redisTemplateJackson.opsForList().size(key);
            //noinspection DataFlowIssue
            if (size > 500) {
                redisTemplateJackson.opsForList().rightPop(key);
            }
        });
    }
}
