package org.javamaster.invocationlab.admin.service.impl;

import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.enums.SqlTypeEnum;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.CommonErdVo;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.ExecuteHistoryBean;
import org.javamaster.invocationlab.admin.model.erd.PropertiesBean;
import org.javamaster.invocationlab.admin.model.erd.SqlExecResVo;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.javamaster.invocationlab.admin.model.erd.TableData;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.DbService;
import org.javamaster.invocationlab.admin.util.DbUtils;
import org.javamaster.invocationlab.admin.util.ExecutorUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.javamaster.invocationlab.admin.util.SessionUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX;
import static org.javamaster.invocationlab.admin.consts.ErdConst.QUERY_RES;
import static org.javamaster.invocationlab.admin.util.DbUtils.getTableName;
import static org.javamaster.invocationlab.admin.util.JsonUtils.STANDARD_PATTERN;

@Slf4j
public abstract class AbstractDbService implements DbService {
    protected final AtomicInteger atomicInteger = new AtomicInteger(0);
    protected final String KEY = "key";
    protected final String INDEX = "index";
    protected final String NULL_VALUE = "<null>";
    protected final String ROW_OPERATION_TYPE = "rowOperationType";

    @Override
    public void checkDb(DbsBean dbsBean) {
        try {
            PropertiesBean propertiesBean = dbsBean.getProperties();
            String urlDbName = DbUtils.resolveUrlDbName(propertiesBean.getUrl());
            JdbcTemplate jdbcTemplate = DbUtils.jdbcTemplateSingleton(propertiesBean, urlDbName);
            jdbcTemplate.execute(getCheckSql());
        } catch (Exception e) {
            throw new ErdException(e.getMessage());
        }
    }

    public abstract String getCheckSql();

    @Override
    public List<String> getDbs(DbsBean dbsBean) {
        PropertiesBean propertiesBean = dbsBean.getProperties();
        String urlDbName = DbUtils.resolveUrlDbName(propertiesBean.getUrl());
        JdbcTemplate jdbcTemplate = DbUtils.jdbcTemplateSingleton(propertiesBean, urlDbName);
        return getDbs(jdbcTemplate);
    }

    public abstract List<String> getDbs(JdbcTemplate jdbcTemplate);

    @Override
    public List<Table> getTables(DbsBean dbsBean, String selectDB) {
        JdbcTemplate jdbcTemplate = DbUtils.jdbcTemplateSingleton(dbsBean.getProperties(), selectDB);
        return getTables(jdbcTemplate, selectDB);
    }

    public abstract List<Table> getTables(JdbcTemplate jdbcTemplate, String selectDB);

    @Override
    public List<SqlExecResVo> execUpdate(DbsBean dbsBean, TokenVo tokenVo, CommonErdVo reqVo) {
        JdbcTemplate jdbcTemplate = DbUtils.jdbcTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());
        TransactionTemplate transactionTemplate = DbUtils.transactionTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());
        return execUpdate(jdbcTemplate, transactionTemplate, tokenVo, reqVo);
    }

    protected abstract List<String> getPrimaryKeys(JdbcTemplate jdbcTemplate, String selectDB, String tableName);

    protected List<SqlExecResVo> execUpdate(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate,
                                            TokenVo tokenVo, CommonErdVo reqVo) {
        String tableName = getTableName(reqVo.getSql());
        List<String> primaryKeyNames = getPrimaryKeys(jdbcTemplate, reqVo.getSelectDB(), tableName);
        return transactionTemplate.execute(transactionStatus -> reqVo.getRows().stream()
                .map(tmpRow -> {
                    // 过滤掉值为null的列
                    JSONObject row = JsonUtils.parseObject(JsonUtils.objectToString(tmpRow), JSONObject.class);
                    int index = row.getIntValue(INDEX);
                    Pair<String, List<Object>> pairPrimary = primaryKeyConditions(row, primaryKeyNames);
                    String sqlTmp;
                    List<Object> fieldValues;
                    Pair<String, List<Object>> tmpPair;
                    String rowOperationType = row.getString(ROW_OPERATION_TYPE);
                    SqlTypeEnum sqlTypeEnum = SqlTypeEnum.getByType(rowOperationType);
                    if (sqlTypeEnum == SqlTypeEnum.DELETE) {
                        tmpPair = handleDeleteOperation(Objects.requireNonNull(pairPrimary), row, tableName);
                    } else if (sqlTypeEnum == SqlTypeEnum.UPDATE) {
                        tmpPair = handleEditOperation(pairPrimary, row, tableName);
                    } else {
                        tmpPair = handleInsertOperation(row, tableName);
                    }
                    sqlTmp = tmpPair.getLeft();
                    fieldValues = tmpPair.getRight();
                    return executeDangerousSql(jdbcTemplate, sqlTmp, tokenVo, reqVo.getQueryId(), fieldValues, index);
                })
                .collect(Collectors.toList()));
    }

    protected abstract Pair<String, List<Object>> handleDeleteOperation(Pair<String, List<Object>> pairPrimary, JSONObject row, String tableName);

    protected abstract Pair<String, List<Object>> handleEditOperation(Pair<String, List<Object>> pairPrimary, JSONObject row, String tableName);

    protected abstract Pair<String, List<Object>> handleInsertOperation(JSONObject row, String tableName);

    private SqlExecResVo executeDangerousSql(JdbcTemplate jdbcTemplate, String sql, TokenVo tokenVo,
                                             String queryId, List<Object> sqlValues, int index) {
        log.info("{}-{} execute dml sql:{},params:{}", tokenVo.getUserId(), tokenVo.getUsername(), sql, sqlValues);
        long start = System.currentTimeMillis();
        int num = jdbcTemplate.update(sql, sqlValues.toArray());
        long end = System.currentTimeMillis();
        log.info("{}-{} execute dml sql res num:{}", tokenVo.getUserId(), tokenVo.getUsername(), num);

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setColumns(Sets.newHashSet("affect_num"));
        resVo.setTableColumns(Collections.emptyMap());
        resVo.setPrimaryKeys(Collections.emptyList());
        resVo.setQueryKey(atomicInteger.incrementAndGet());

        TableData tableData = new TableData();
        tableData.setTotal(1);

        Map<String, Object> map = Maps.newHashMap();
        map.put("affect_num", num);
        //noinspection unchecked
        tableData.setRecords(Lists.newArrayList(map));

        resVo.setTableData(tableData);

        if (num != 1) {
            addSqlToExecuteHistory(sql + " (已回滚)", jdbcTemplate, tokenVo, queryId, sqlValues, end - start);
            throw new ErdException("第 " + (index + 1) + " 行记录根据结果集条件不能确定唯一的记录,期望影响 1 条,实际会影响 " + num + " 条!");
        } else {
            addSqlToExecuteHistory(sql, jdbcTemplate, tokenVo, queryId, sqlValues, end - start);
        }
        return resVo;
    }

    @Override
    public List<Column> getTableColumns(DbsBean dbsBean, String selectDB, String tableName) {
        JdbcTemplate jdbcTemplate = DbUtils.jdbcTemplateSingleton(dbsBean.getProperties(), selectDB);
        return getTableColumns(jdbcTemplate, selectDB, tableName);
    }

    public abstract List<Column> getTableColumns(JdbcTemplate jdbcTemplate, String selectDB, String tableName);

    protected boolean isDmlSql(String sql) {
        String lowerSql = sql.toLowerCase();
        return lowerSql.startsWith(SqlTypeEnum.UPDATE.type)
                || lowerSql.startsWith(SqlTypeEnum.DELETE.type)
                || lowerSql.startsWith(SqlTypeEnum.INSERT.type);
    }

    protected boolean isDdlSql(String sql) {
        String lowerSql = sql.toLowerCase();
        return lowerSql.startsWith(SqlTypeEnum.CREATE.type)
                || lowerSql.startsWith(SqlTypeEnum.ALTER.type)
                || lowerSql.startsWith(SqlTypeEnum.DROP.type);
    }

    @Override
    public SqlExecResVo execSql(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo) {
        boolean useNoneDefaultDb = !dbsBean.getName().equals(reqVo.getSelectDB());

        JdbcTemplate jdbcTemplate = DbUtils.jdbcTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());
        if (reqVo.getExplain()) {
            return executeExplainSql(reqVo, jdbcTemplate, tokenVo);
        }

        boolean ddlSql = isDdlSql(reqVo.getSql());
        if (isDmlSql(reqVo.getSql()) || ddlSql) {
            TransactionTemplate transactionTemplate = DbUtils.transactionTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());
            return transactionTemplate.execute(status -> executeDangerousSql(jdbcTemplate, tokenVo, ddlSql, reqVo));
        }

        return executeDqlSql(jdbcTemplate, tokenVo, useNoneDefaultDb, reqVo);
    }

    protected abstract SqlExecResVo executeExplainSql(CommonErdVo reqVo, JdbcTemplate jdbcTemplate, TokenVo tokenVo);

    protected abstract SqlExecResVo executeDangerousSql(JdbcTemplate jdbcTemplate, TokenVo tokenVo,
                                                        boolean ddlSql, CommonErdVo reqVo);

    protected abstract SqlExecResVo executeDqlSql(JdbcTemplate jdbcTemplate, TokenVo tokenVo,
                                                  boolean useNoneDefaultDb, CommonErdVo reqVo);

    protected Map<String, Object> getOldQueryMap(Object rowUniqueKey) {
        List<Map<String, Object>> oldRecords = SessionUtils.getFromSession(QUERY_RES);
        if (oldRecords == null) {
            throw new ErdException("请重新刷新查询后再重试");
        }
        List<Map<String, Object>> list = oldRecords.stream()
                .filter(oldRecord -> oldRecord.get(KEY).equals(rowUniqueKey))
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
     * @param primaryKeyColumns 主键名列表
     * @return 返回主键信息, 格式如 Pair.of(<"id = ? and secondId = ?", [1, 2]>)
     */
    protected Pair<String, List<Object>> primaryKeyConditions(JSONObject row, List<String> primaryKeyColumns) {
        Map<String, Object> oldRow = getOldQueryMap(row.get(KEY));
        if (oldRow == null) {
            return null;
        }
        List<Object> primaryKeyValues = primaryKeyColumns.stream()
                .map(oldRow::get)
                .collect(Collectors.toList());
        String key = primaryKeyColumns.stream()
                .map(primaryKey -> primaryKey + "=?")
                .collect(Collectors.joining(" and "));
        return Pair.of(key, primaryKeyValues);
    }

    protected boolean filterUnRelateColumn(Map.Entry<String, Object> entry) {
        return !ROW_OPERATION_TYPE.equals(entry.getKey())
                && !KEY.equals(entry.getKey())
                && !INDEX.equals(entry.getKey());
    }

    protected boolean filterUnChangeColumn(Map.Entry<String, Object> entry, Map<String, Object> queryMap) {
        String columnName = entry.getKey();
        Object columnValue = entry.getValue();
        Object dbColumnValue = queryMap.get(columnName);
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
        JdbcTemplate jdbcTemplate = DbUtils.jdbcTemplateSingleton(dbsBean.getProperties(), reqVo.getSelectDB());
        return exportSql(jdbcTemplate, dbsBean, tokenVo, reqVo);
    }

    protected Triple<String, MediaType, byte[]> exportSql(JdbcTemplate jdbcTemplate, DbsBean dbsBean,
                                                          TokenVo tokenVo, CommonErdVo reqVo) {
        PlainSelect plainSelect = DbUtils.parseSql(reqVo.getSql());
        Pair<String, Triple<Boolean, String, List<String>>> pair = modifyQuerySql(plainSelect, reqVo.getSql(),
                jdbcTemplate, reqVo);
        List<String> primaryKeys = pair.getRight().getRight();
        if (primaryKeys.isEmpty() && reqVo.getType().contains("sql")) {
            throw new ErdException("不支持的操作");
        }

        SqlExecResVo resVo;
        List<Map<String, Object>> oldRecords = SessionUtils.getFromSession(QUERY_RES);
        try {
            resVo = execSql(reqVo, dbsBean, tokenVo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SessionUtils.saveToSession(QUERY_RES, oldRecords);

        List<Map<String, Object>> records = resVo.getTableData().getRecords();
        records.forEach(record -> {
            record.remove(KEY);
            record.remove(INDEX);
        });

        String type = reqVo.getType();
        String sql = reqVo.getSql();
        String fileName = "SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + "." + reqVo.getType();
        if ("json".equals(reqVo.getType())) {
            String jsonStr = JsonUtils.objectToString(records);
            return Triple.of(fileName, MediaType.TEXT_PLAIN, jsonStr.getBytes(StandardCharsets.UTF_8));
        } else {
            String tableName = getTableName(plainSelect);
            if ("xls".equals(type)) {

                byte[] bytes = xlsBytes(records, tableName);
                return Triple.of(fileName, new MediaType("application", "vnd.ms-excel"), bytes);

            } else if ("sqlInsert".equals(type)) {

                fileName = "SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + ".sql";
                String tmpSql = generateInsertSql(records, sql, plainSelect);
                return Triple.of(fileName, MediaType.TEXT_PLAIN, tmpSql.getBytes(StandardCharsets.UTF_8));

            } else if ("sqlUpdate".equals(type)) {

                fileName = "SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + ".sql";
                List<Column> primaryColumns = getPrimaryColumns(jdbcTemplate, reqVo.getSelectDB(), tableName);
                String tmpSql = generateUpdateSql(records, sql, plainSelect, primaryColumns);
                return Triple.of(fileName, MediaType.TEXT_PLAIN, tmpSql.getBytes(StandardCharsets.UTF_8));

            } else if ("csv".equals(type)) {

                String s = generateCsv(records, sql, plainSelect);
                return Triple.of(fileName, MediaType.TEXT_PLAIN, s.getBytes(StandardCharsets.UTF_8));

            }
        }
        throw new RuntimeException(type);
    }

    protected abstract List<Column> getPrimaryColumns(JdbcTemplate jdbcTemplate, String selectDB, String tableName);

    protected abstract String generateCsv(List<Map<String, Object>> records, String sql, PlainSelect plainSelect);

    protected abstract String generateUpdateSql(List<Map<String, Object>> records, String sql, PlainSelect plainSelect, List<Column> primaryColumns);

    protected abstract String generateInsertSql(List<Map<String, Object>> records, String sql, PlainSelect plainSelect);

    protected abstract byte[] xlsBytes(List<Map<String, Object>> records, String tableName);

    protected abstract Pair<String, Triple<Boolean, String, List<String>>> modifyQuerySql(PlainSelect plainSelect,
                                                                                          String sql,
                                                                                          JdbcTemplate jdbcTemplate,
                                                                                          CommonErdVo reqVo);

    protected List<String> getColumnNames(List<Map<String, Object>> records, String sql, PlainSelect plainSelect) {
        List<String> columnNames;
        if (sql.contains("*")) {
            columnNames = new ArrayList<>(records.get(0).keySet());
        } else {
            columnNames = plainSelect.getSelectItems().stream()
                    .map(selectItem -> {
                        SelectExpressionItem item = (SelectExpressionItem) selectItem;
                        if (item.getAlias() != null && StringUtils.isNotBlank(item.getAlias().getName())) {
                            return item.getAlias().getName();
                        }
                        return ((net.sf.jsqlparser.schema.Column) item.getExpression()).getColumnName();
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

    protected void addSqlToExecuteHistory(String sql, JdbcTemplate jdbcTemplate, TokenVo tokenVo, String queryId,
                                          List<Object> sqlValues, long duration) {
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
