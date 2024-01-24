package org.javamaster.invocationlab.admin.service.impl;

import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.CommonErdVo;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.SqlExecResVo;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.javamaster.invocationlab.admin.model.erd.TableData;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.util.DbUtils;
import org.javamaster.invocationlab.admin.util.SessionUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.consts.ErdConst.QUERY_RES;
import static org.javamaster.invocationlab.admin.util.DbUtils.getTableName;

@Slf4j
public class PostgreSqlDbServiceImpl extends AbstractDbService {
    @Override
    public String getCheckSql() {
        return "select now()";
    }

    @Override
    public List<String> getDbs(JdbcTemplate jdbcTemplate) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT datname FROM pg_database");
        return list.stream()
                .map(it -> it.get("datname").toString())
                .collect(Collectors.toList());
    }

    @Override
    public List<Table> getTables(JdbcTemplate jdbcTemplate, String selectDB) {
        String sql = "SELECT * FROM pg_tables WHERE tableowner = '" + selectDB + "'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return list.stream()
                .map(map -> {
                    String tableName = (String) map.get("tablename");
                    String remarks = (String) map.getOrDefault("table_comment", "-");
                    return new Table(tableName, remarks);
                })
                .collect(Collectors.toList());
    }

    @Override
    protected List<String> getPrimaryKeys(JdbcTemplate jdbcTemplate, String selectDB, String tableName) {
        List<Column> tableColumns = DbUtils.getTableColumns(jdbcTemplate, tableName);
        return tableColumns.stream()
                .filter(Column::isPrimaryKey)
                .map(Column::getName)
                .collect(Collectors.toList());
    }

    @Override
    protected Pair<String, List<Object>> handleDeleteOperation(Pair<String, List<Object>> pairPrimary, JSONObject row, String tableName) {
        return null;
    }

    @Override
    protected Pair<String, List<Object>> handleEditOperation(Pair<String, List<Object>> pairPrimary, JSONObject row, String tableName) {
        return null;
    }

    @Override
    protected Pair<String, List<Object>> handleInsertOperation(JSONObject row, String tableName) {
        return null;
    }

    @Override
    public List<Column> getTableColumns(JdbcTemplate jdbcTemplate, String selectDB, String tableName) {
        String sql = String.format("SELECT * FROM information_schema.columns WHERE table_name = '%s' and table_catalog='%s'",
                tableName, selectDB);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return list.stream()
                .map(map -> {
                    Column column = new Column();
                    column.setName(map.get("column_name").toString());
                    column.setTypeName(map.get("data_type").toString());
                    column.setRemarks("");
                    column.setIsNullable(map.get("is_nullable").toString());
                    column.setPrimaryKey("YES".equals(map.get("is_identity")));
                    column.setDef("");
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
        return null;
    }

    @Override
    protected SqlExecResVo executeDangerousSql(JdbcTemplate jdbcTemplate, TokenVo tokenVo, boolean ddlSql, CommonErdVo reqVo) {
        return null;
    }

    @Override
    protected SqlExecResVo executeDqlSql(JdbcTemplate jdbcTemplate, TokenVo tokenVo, boolean useNoneDefaultDb, CommonErdVo reqVo) {
        String tmpSql = reqVo.getSql().replace(";", "");
        Pair<String, Triple<Boolean, String, List<String>>> triple = Pair.of(tmpSql,
                Triple.of(false, null, Lists.newArrayList()));
        PlainSelect plainSelect;
        try {
            plainSelect = DbUtils.parseSql(tmpSql);
            triple = modifyQuerySql(plainSelect, tmpSql, jdbcTemplate, reqVo);
        } catch (Exception e) {
            log.warn("modify sql error:{}", e.getMessage());
        }
        tmpSql = triple.getLeft();
        boolean showPagination = triple.getRight().getLeft();
        List<String> primaryKeys = triple.getRight().getRight();
        log.info("{}-{} execute dql sql:{}", tokenVo.getUserId(), tokenVo.getUsername(), tmpSql);
        long start = System.currentTimeMillis();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(tmpSql);
        long end = System.currentTimeMillis();
        log.info("{}-{} execute dql sql res size:{}", tokenVo.getUserId(), tokenVo.getUsername(), list.size());
        Set<String> columns = Sets.newLinkedHashSet();
        String tableName = triple.getRight().getMiddle();
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
            }
        }

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setColumns(columns);
        resVo.setQueryKey(atomicInteger.incrementAndGet());
        resVo.setPage(resVo.getPage());
        resVo.setPageSize(resVo.getPageSize());
        resVo.setShowPagination(showPagination);
        resVo.setTableName(tableName);
        resVo.setPrimaryKeys(primaryKeys);

        TableData tableData = new TableData();
        tableData.setTotal(21);
        tableData.setRecords(list);

        resVo.setTableData(tableData);

        if ((useNoneDefaultDb || SpringUtils.isProEnv()) && StringUtils.isNotBlank(tableName)) {
            List<Column> tableColumns = Lists.newArrayList();
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
    protected List<Column> getPrimaryColumns(JdbcTemplate jdbcTemplate, String selectDB, String tableName) {
        return null;
    }

    @Override
    protected String generateCsv(List<Map<String, Object>> records, String sql, PlainSelect plainSelect) {
        return null;
    }

    @Override
    protected String generateUpdateSql(List<Map<String, Object>> records, String sql, PlainSelect plainSelect, List<Column> primaryColumns) {
        return null;
    }

    @Override
    protected String generateInsertSql(List<Map<String, Object>> records, String sql, PlainSelect plainSelect) {
        return null;
    }

    @Override
    protected byte[] xlsBytes(List<Map<String, Object>> records, String tableName) {
        return new byte[0];
    }

    @Override
    protected Pair<String, Triple<Boolean, String, List<String>>> modifyQuerySql(PlainSelect plainSelect, String sql,
                                                                                 JdbcTemplate jdbcTemplate, CommonErdVo reqVo) {
        boolean noLimit = !sql.toLowerCase().contains("limit");
        List<String> tablePrimaryKeys = Lists.newArrayList();
        String tableName = null;
        if (plainSelect.getGroupBy() == null && CollectionUtils.isEmpty(plainSelect.getJoins())) {
            tableName = getTableName(plainSelect);
        }
        return Pair.of(sql, Triple.of(noLimit, tableName, tablePrimaryKeys));
    }

    @Override
    public ModulesBean refreshModule(ErdOnlineModel erdOnlineModel, String moduleName) {
        return null;
    }

    @Override
    public Integer getTableRecordTotal(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo) {
        return null;
    }
}
