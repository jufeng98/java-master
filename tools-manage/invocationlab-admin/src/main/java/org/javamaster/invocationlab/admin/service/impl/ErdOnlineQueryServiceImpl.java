package org.javamaster.invocationlab.admin.service.impl;

import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.consts.ErdConst;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.ExecuteHistoryBean;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.Tree;
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService;
import org.javamaster.invocationlab.admin.service.ErdOnlineQueryService;
import org.javamaster.invocationlab.admin.util.DbUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX;
import static org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_QUERY_TREE;
import static org.javamaster.invocationlab.admin.consts.ErdConst.QUERY_TREE_TREE_NODE;
import static org.javamaster.invocationlab.admin.util.DbUtils.executeAndResetDefaultDb;
import static org.javamaster.invocationlab.admin.util.DbUtils.getTableName;
import static org.javamaster.invocationlab.admin.util.JsonUtils.STANDARD_PATTERN;

/**
 * @author yudong
 */
@SuppressWarnings("unchecked")
@Service
@Slf4j
public class ErdOnlineQueryServiceImpl implements ErdOnlineQueryService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplateJackson;
    @Autowired
    private ErdOnlineProjectService erdProjectService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    private final AtomicInteger atomicInteger = new AtomicInteger(0);
    public static final String NULL_VALUE = "<null>";
    public static final String INDEX = "index";
    public static final String ROW_OPERATION_TYPE = "rowOperationType";

    @Override
    public JSONArray load(JSONObject jsonObject) {
        String projectId = jsonObject.getString("projectId");
        String jsonStr = (String) stringRedisTemplate.opsForHash().get(ERD_PREFIX + "load", projectId);
        if (jsonStr == null) {
            return new JSONArray();
        }
        return JSONObject.parseArray(jsonStr);
    }

    @Override
    public List<Tree> getQueryTreeList(String projectId) throws Exception {
        String jsonDataStr = (String) stringRedisTemplate.opsForHash().get(PROJECT_QUERY_TREE, projectId);
        if (jsonDataStr == null) {
            return Lists.newArrayList();
        }
        return objectMapper.readValue(jsonDataStr, new TypeReference<List<Tree>>() {
        });
    }

    @Override
    public Boolean createDirOrQuery(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        Boolean isLeaf = jsonObjectReq.getBoolean("isLeaf");
        String title = jsonObjectReq.getString("title");
        String parentId = jsonObjectReq.getString("parentId");
        List<Tree> trees = getQueryTreeList(projectId);
        Tree tree = new Tree();
        tree.setId(RandomUtils.nextLong(10000000000L, 99999999999L) + "");
        tree.setKey(tree.getId());
        tree.setValue(tree.getId());
        tree.setTitle(title);
        tree.setLabel(title);
        tree.setIsLeaf(isLeaf);
        tree.setChildren(Lists.newArrayList());
        if (StringUtils.isNotBlank(parentId)) {
            //noinspection OptionalGetWithoutIsPresent
            Tree parent = trees.stream().filter(tree1 -> tree1.getId().equals(parentId)).findAny().get();
            tree.setParentId(tree.getId());
            tree.setParentKey(tree.getKey());
            tree.setParentKeys(Lists.newArrayList(null, null));
            parent.getChildren().add(tree);
        } else {
            tree.setParentKeys(Lists.newArrayList((String) null));
            trees.add(tree);
        }
        stringRedisTemplate.opsForHash().put(PROJECT_QUERY_TREE, projectId, objectMapper.writeValueAsString(trees));
        stringRedisTemplate.opsForHash().put(QUERY_TREE_TREE_NODE, tree.getId(), objectMapper.writeValueAsString(tree));
        return true;
    }

    @Override
    public Tree queryTree(String treeNodeId) throws Exception {
        String jsonStrData = (String) stringRedisTemplate.opsForHash().get(QUERY_TREE_TREE_NODE, treeNodeId);
        return objectMapper.readValue(jsonStrData, Tree.class);
    }

    @Override
    public Integer deleteQueryTree(String projectId, String treeNodeId) throws Exception {
        String jsonStrData = (String) stringRedisTemplate.opsForHash().get(PROJECT_QUERY_TREE, projectId);
        List<Tree> trees = objectMapper.readValue(jsonStrData, new TypeReference<List<Tree>>() {
        });
        boolean del = trees.removeIf(tree -> tree.getId().equals(treeNodeId));
        if (!del) {
            trees.forEach(tree -> tree.getChildren().removeIf(children -> children.getId().equals(treeNodeId)));
        }
        stringRedisTemplate.opsForHash().put(PROJECT_QUERY_TREE, projectId, objectMapper.writeValueAsString(trees));
        return stringRedisTemplate.opsForHash().delete(QUERY_TREE_TREE_NODE, treeNodeId).intValue();
    }

    @Override
    public List<String> getDbs(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        JdbcTemplate jdbcTemplate = getJdbcTemplate(projectId, tokenVo);
        String defaultDbName = (String) jdbcTemplate.queryForMap("select database()").get("database()");
        List<String> dbs = jdbcTemplate.queryForList("show databases").stream()
                .map(map -> map.get("Database").toString())
                .collect(Collectors.toList());
        dbs.remove(defaultDbName);
        dbs.add(0, defaultDbName);
        return dbs;
    }

    @Override
    public List<Table> getTables(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        String selectDB = jsonObjectReq.getString("selectDB");
        JdbcTemplate jdbcTemplate = getJdbcTemplate(projectId, tokenVo);
        return executeAndResetDefaultDb(jdbcTemplate, selectDB, () -> DbUtils.getTables(selectDB, jdbcTemplate)
        );
    }

    @Override
    public List<Column> getTableColumns(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        String selectDB = jsonObjectReq.getString("selectDB");
        String tableName = jsonObjectReq.getString("tableName");
        JdbcTemplate jdbcTemplate = getJdbcTemplate(projectId, tokenVo);
        return DbUtils.executeAndResetDefaultDb(jdbcTemplate, selectDB,
                () -> DbUtils.getTableColumns1(jdbcTemplate, tableName));
    }

    @Override
    public JSONObject queryHistory(String queryId, TokenVo tokenVo) {
        String key = ERD_PREFIX + "sqlHistory:" + queryId;
        Long size = redisTemplateJackson.opsForList().size(key);
        List<Object> range = redisTemplateJackson.opsForList().range(key, 0, 99);
        JSONObject tableData = new JSONObject();
        tableData.put("records", range);
        tableData.put("total", size);
        return tableData;
    }

    @Override
    public Tree saveQueryTree(JSONObject jsonObjectReq, String treeNodeId, TokenVo tokenVo) throws Exception {
        String jsonStrData = (String) stringRedisTemplate.opsForHash().get(QUERY_TREE_TREE_NODE, treeNodeId);
        Tree tree = objectMapper.readValue(jsonStrData, Tree.class);
        String projectId = jsonObjectReq.getString("projectId");
        String sqlInfo = jsonObjectReq.getString("sqlInfo");
        String selectDB = jsonObjectReq.getString("selectDB");
        if (StringUtils.isNotBlank(sqlInfo)) {
            tree.setSqlInfo(sqlInfo);
            tree.setSelectDB(selectDB);
        }
        String title = jsonObjectReq.getString("title");
        if (StringUtils.isNotBlank(title)) {
            tree.setTitle(title);
            tree.setLabel(title);
        }
        stringRedisTemplate.opsForHash().put(QUERY_TREE_TREE_NODE, treeNodeId, objectMapper.writeValueAsString(tree));

        List<Tree> queryTreeList = getQueryTreeList(projectId);
        modifyTreeList(queryTreeList, tree);

        stringRedisTemplate.opsForHash().put(PROJECT_QUERY_TREE, projectId, objectMapper.writeValueAsString(queryTreeList));
        return tree;
    }

    private void modifyTreeList(List<Tree> queryTreeList, Tree tree) {
        queryTreeList
                .forEach(it -> {
                    if (tree.getIsLeaf()) {
                        it.getChildren()
                                .forEach(innerIt -> {
                                    if (innerIt.getId().equals(tree.getId())) {
                                        BeanUtils.copyProperties(tree, innerIt);
                                    }
                                });
                    } else {
                        if (it.getId().equals(tree.getId())) {
                            it.setSqlInfo(tree.getSqlInfo());
                            it.setSelectDB(tree.getSelectDB());
                            it.setLabel(tree.getLabel());
                            it.setTitle(tree.getTitle());
                        }
                    }
                });
    }

    public DbsBean getDefaultDb(String projectId, TokenVo tokenVo) throws Exception {
        DbsBean defaultDb = (DbsBean) redisTemplateJackson.opsForHash().get(ErdConst.PROJECT_DS, projectId);
        if (defaultDb != null) {
            return defaultDb;
        }
        ErdOnlineModel erdOnlineModel = erdProjectService.getProjectDetail(projectId, tokenVo);
        erdProjectService.resumeSensitiveInfo(erdOnlineModel, tokenVo.getUserId());
        return DbUtils.getDefaultDb(erdOnlineModel);
    }

    private JdbcTemplate getJdbcTemplate(DbsBean defaultDb) {
        return DbUtils.jdbcTemplate(defaultDb.getProperties());
    }

    private JdbcTemplate getJdbcTemplate(String projectId, TokenVo tokenVo) throws Exception {
        DbsBean defaultDb = getDefaultDb(projectId, tokenVo);
        return DbUtils.jdbcTemplate(defaultDb.getProperties());
    }

    @Override
    public JSONObject execSql(JSONObject jsonObjectReq, Boolean explain, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        Integer page = jsonObjectReq.getInteger("page");
        Integer pageSize = jsonObjectReq.getInteger("pageSize");
        boolean isExport = jsonObjectReq.getBooleanValue("isExport");
        String selectDB = jsonObjectReq.getString("dbName");

        DbsBean defaultDb = getDefaultDb(projectId, tokenVo);
        JdbcTemplate jdbcTemplate = getJdbcTemplate(defaultDb);
        boolean useNoneDefaultDb = !defaultDb.getName().equals(selectDB);

        return executeAndResetDefaultDb(jdbcTemplate, selectDB, () -> {
            String lowerSql = jsonObjectReq.getString("sql").trim().toLowerCase();
            String queryId = jsonObjectReq.getString("queryId");
            lowerSql = lowerSql.replace(";", "");
            if (explain) {
                return explainRes(lowerSql, jdbcTemplate, tokenVo, queryId);
            }
            if (lowerSql.startsWith("update") || lowerSql.startsWith("delete")) {
                return updateOrDelRes(lowerSql, jdbcTemplate, tokenVo, queryId, Collections.emptyList());
            }
            return queryRes(lowerSql, jdbcTemplate, tokenVo, queryId, page, pageSize, isExport, useNoneDefaultDb);
        });
    }

    private boolean filterUnRelateColumn(Map.Entry<String, Object> entry) {
        return !ROW_OPERATION_TYPE.equals(entry.getKey())
                && !INDEX.equals(entry.getKey());
    }

    private boolean filterUnChangeColumn(Map.Entry<String, Object> entry, Map<String, Object> queryMap) {
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
    public List<JSONObject> execUpdate(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        JdbcTemplate jdbcTemplate = getJdbcTemplate(projectId, tokenVo);
        String queryId = jsonObjectReq.getString("queryId");
        String sqlTmp = jsonObjectReq.getString("sql");
        String selectDB = jsonObjectReq.getString("dbName");
        return DbUtils.executeAndResetDefaultDb(jdbcTemplate, selectDB, () -> {
            Pair<String, List<String>> pair = DbUtils.getTableNameAndPrimaryKey(jdbcTemplate, sqlTmp);
            String tableName = pair.getLeft();
            List<String> primaryKeyColumn = pair.getRight();
            String primaryKey = primaryKeyColumn.get(0);
            JSONArray rows = jsonObjectReq.getJSONArray("rows");
            return rows.stream().map(row -> {
                // 过滤掉值为null的列
                JSONObject jsonObject = JsonUtils.parseObject(JsonUtils.objectToString(row), JSONObject.class);
                Object primaryKeyValue = jsonObject.get(primaryKey);
                String sql;
                List<Object> fieldValues = Lists.newArrayList();
                String rowOperationType = jsonObject.getString(ROW_OPERATION_TYPE);
                if ("delete".equals(rowOperationType)) {
                    sql = String.format("delete from %s where %s=?", tableName, primaryKey);
                    fieldValues.add(primaryKeyValue);
                } else if ("edit".equals(rowOperationType)) {
                    String querySql = String.format("select * from %s where %s=?", tableName, primaryKey);
                    Map<String, Object> queryMap = jdbcTemplate.queryForMap(querySql, primaryKeyValue);
                    String fields = jsonObject.entrySet().stream()
                            .filter(entry -> filterUnRelateColumn(entry) && !primaryKey.equals(entry.getKey()))
                            .filter(entry -> filterUnChangeColumn(entry, queryMap))
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
                        throw new ErdException("数据无变化,未做任何更新操作");
                    }
                    sql = String.format("update %s set %s where %s=?", tableName, fields, primaryKey);
                    fieldValues.add(primaryKeyValue);
                } else {
                    List<String> names = Lists.newArrayList();
                    jsonObject.entrySet().stream()
                            .filter(this::filterUnRelateColumn).forEach(entry -> {
                                names.add(entry.getKey());
                                if (NULL_VALUE.equals(entry.getValue())) {
                                    fieldValues.add(null);
                                } else {
                                    fieldValues.add(entry.getValue());
                                }
                            });
                    sql = String.format("insert into %s (%s) values (%s)", tableName, String.join(",", names), names.stream()
                            .map(name -> "?")
                            .collect(Collectors.joining(",")));
                }
                return updateOrDelRes(sql, jdbcTemplate, tokenVo, queryId, fieldValues);
            }).collect(Collectors.toList());
        });
    }

    private JSONObject explainRes(String sql, JdbcTemplate jdbcTemplate, TokenVo tokenVo, String queryId) {
        sql = "desc " + sql;
        long start = System.currentTimeMillis();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        long end = System.currentTimeMillis();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("columns", list.get(0).keySet());
        jsonObject.put("tableData", list);
        addSqlToExecuteHistory(sql, jdbcTemplate, tokenVo, queryId, Collections.emptyList(), end - start);
        return jsonObject;
    }

    private void addSqlToExecuteHistory(String sql, JdbcTemplate jdbcTemplate, TokenVo tokenVo, String queryId,
                                        List<Object> sqlValues, long duration) {
        Map<String, Object> map = jdbcTemplate.queryForMap("select database()");
        String dbName = (String) map.get("database()");
        String key = ERD_PREFIX + "sqlHistory:" + queryId;
        ExecuteHistoryBean bean = new ExecuteHistoryBean();
        bean.setSqlInfo(sql);
        bean.setDbName(dbName);
        bean.setDuration(duration);
        bean.setCreateTime(new Date());
        bean.setCreator(tokenVo.getUserId());
        bean.setParams(sqlValues.toString());
        redisTemplateJackson.opsForList().leftPush(key, bean);
        Long size = redisTemplateJackson.opsForList().size(key);
        //noinspection DataFlowIssue,ConstantConditions
        if (size > 200) {
            redisTemplateJackson.opsForList().rightPop(key);
        }
    }

    private JSONObject updateOrDelRes(String lowerSql, JdbcTemplate jdbcTemplate, TokenVo tokenVo,
                                      String queryId, List<Object> sqlValues) {
        if (!lowerSql.startsWith("insert") && !lowerSql.contains("where")) {
            throw new ErdException("语句缺少where条件");
        }
        long start = System.currentTimeMillis();
        int num;
        if (CollectionUtils.isEmpty(sqlValues)) {
            num = jdbcTemplate.update(lowerSql);
        } else {
            num = jdbcTemplate.update(lowerSql, sqlValues.toArray());
        }
        log.info("execute sql:{},num:{}", lowerSql, num);
        long end = System.currentTimeMillis();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("columns", Lists.newArrayList("affect_num"));
        JSONObject tableData = new JSONObject();
        jsonObject.put("tableData", tableData);
        Map<String, Object> map = Maps.newHashMap();
        map.put("affect_num", num);
        tableData.put("records", Lists.newArrayList(map));
        tableData.put("total", 1);
        addSqlToExecuteHistory(lowerSql, jdbcTemplate, tokenVo, queryId, sqlValues, end - start);
        return jsonObject;
    }

    @SuppressWarnings({"ConstantConditions"})
    private Triple<String, Long, Triple<Boolean, String, List<String>>> modifyQuerySql(PlainSelect plainSelect, String lowerSql,
                                                                                       JdbcTemplate jdbcTemplate, Integer page,
                                                                                       Integer pageSize) {
        Long count;
        int offset = (page - 1) * pageSize;
        boolean noLimit = !lowerSql.contains("limit");
        List<String> tablePrimaryKeys = Lists.newArrayList();
        String tableName = null;
        if (plainSelect.getGroupBy() != null) {
            String countSql = "select count(*) from (" + lowerSql + ") tmp";
            count = jdbcTemplate.queryForObject(countSql, Long.class);
            if (noLimit) {
                lowerSql += " limit " + offset + "," + pageSize;
            }
        } else {
            int fromIndex = lowerSql.indexOf("from");
            String countSql = "SELECT count(*) " + lowerSql.substring(fromIndex);
            count = jdbcTemplate.queryForObject(countSql, Long.class);
            if (noLimit) {
                lowerSql += " limit " + offset + "," + pageSize;
            }
            if (CollectionUtils.isEmpty(plainSelect.getJoins())) {
                tableName = getTableName(plainSelect);
                tablePrimaryKeys = DbUtils.getTablePrimaryColumns1(jdbcTemplate, tableName).stream()
                        .map(Column::getName)
                        .collect(Collectors.toList());
            }
        }
        return Triple.of(lowerSql, count, Triple.of(noLimit, tableName, tablePrimaryKeys));
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

    private JSONObject queryRes(String lowerSql, JdbcTemplate jdbcTemplate, TokenVo tokenVo, String queryId, Integer page,
                                Integer pageSize, boolean isExport, boolean useNoneDefaultDb) {
        Triple<String, Long, Triple<Boolean, String, List<String>>> triple = Triple.of(lowerSql, 0L,
                Triple.of(false, null, Lists.newArrayList()));
        PlainSelect plainSelect;
        try {
            plainSelect = DbUtils.parseSql(lowerSql);
            triple = modifyQuerySql(plainSelect, lowerSql, jdbcTemplate, page, pageSize);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        lowerSql = triple.getLeft();
        Long count = triple.getMiddle();
        boolean showPagination = triple.getRight().getLeft();
        List<String> primaryKeys = triple.getRight().getRight();
        long start = System.currentTimeMillis();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(lowerSql);
        long end = System.currentTimeMillis();
        Set<String> columns = Sets.newLinkedHashSet();
        String tableName = triple.getRight().getMiddle();
        if (list.isEmpty()) {
            if (primaryKeys.size() > 0) {
                List<Column> tableColumns = DbUtils.getTableColumns1(jdbcTemplate, tableName);
                columns.addAll(tableColumns.stream()
                        .map(Column::getName)
                        .collect(Collectors.toList()));
                columns.add(INDEX);
            }
        } else {
            columns.addAll(list.get(0).keySet());
            columns.add(INDEX);
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> rowMap = list.get(i);
                if (!isExport) {
                    modifyRowForSpecialColumnType(rowMap);
                }
                rowMap.put(INDEX, i);
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("columns", columns);
        JSONObject tableData = new JSONObject();
        jsonObject.put("tableData", tableData);
        tableData.put("records", list);
        tableData.put("total", count);
        jsonObject.put("queryKey", atomicInteger.incrementAndGet());
        jsonObject.put("page", page);
        jsonObject.put("pageSize", pageSize);
        jsonObject.put("primaryKeys", primaryKeys);
        jsonObject.put("showPagination", showPagination);
        jsonObject.put("tableName", tableName);
        if (useNoneDefaultDb) {
            List<Column> tableColumns = DbUtils.getTableColumns1(jdbcTemplate, tableName);
            Map<String, Column> map = Maps.uniqueIndex(tableColumns, Column::getName);
            jsonObject.put("tableColumns", map);
        } else {
            jsonObject.put("tableColumns", Collections.emptyMap());
        }
        addSqlToExecuteHistory(lowerSql, jdbcTemplate, tokenVo, queryId, Collections.emptyList(), end - start);
        return jsonObject;
    }

    @Override
    public Triple<String, MediaType, byte[]> exportSql(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        Integer page = jsonObjectReq.getInteger("page");
        Integer pageSize = jsonObjectReq.getInteger("pageSize");
        String lowerSql = jsonObjectReq.getString("sql").trim().toLowerCase();
        String type = jsonObjectReq.getString("type");
        String selectDB = jsonObjectReq.getString("dbName");
        JdbcTemplate jdbcTemplate = getJdbcTemplate(projectId, tokenVo);

        return DbUtils.executeAndResetDefaultDb(jdbcTemplate, selectDB, () -> {
            PlainSelect plainSelect;
            try {
                plainSelect = DbUtils.parseSql(lowerSql);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Triple<String, Long, Triple<Boolean, String, List<String>>> triple = modifyQuerySql(plainSelect, lowerSql,
                    jdbcTemplate, page, pageSize);
            List<String> primaryKeys = triple.getRight().getRight();
            if (primaryKeys.size() == 0 && type.contains("sql")) {
                throw new ErdException("不支持的操作");
            }

            jsonObjectReq.put("isExport", true);
            JSONObject jsonObject;
            try {
                jsonObject = execSql(jsonObjectReq, false, tokenVo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            List<Map<String, Object>> records = (List<Map<String, Object>>) jsonObject.getJSONObject("tableData").get("records");
            records.forEach(record -> record.remove(INDEX));

            String fileName = "SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + "." + type;
            if ("json".equals(type)) {
                String jsonStr = JsonUtils.objectToString(records);
                return Triple.of(fileName, MediaType.TEXT_PLAIN, jsonStr.getBytes(StandardCharsets.UTF_8));
            } else {
                String tableName = getTableName(plainSelect);
                if ("xls".equals(type)) {

                    byte[] bytes = xlsBytes(records, tableName);
                    return Triple.of(fileName, new MediaType("application", "vnd.ms-excel"), bytes);

                } else if ("sqlInsert".equals(type)) {

                    fileName = "SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + ".sql";
                    String sql = generateInsertSql(records, lowerSql, plainSelect);
                    return Triple.of(fileName, MediaType.TEXT_PLAIN, sql.getBytes(StandardCharsets.UTF_8));

                } else if ("sqlUpdate".equals(type)) {

                    fileName = "SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + ".sql";
                    List<Column> primaryColumns = DbUtils.getTablePrimaryColumns(jdbcTemplate, tableName);
                    String sql = generateUpdateSql(records, lowerSql, plainSelect, primaryColumns);
                    return Triple.of(fileName, MediaType.TEXT_PLAIN, sql.getBytes(StandardCharsets.UTF_8));

                } else if ("csv".equals(type)) {

                    String s = generateCsv(records, lowerSql, plainSelect);
                    return Triple.of(fileName, MediaType.TEXT_PLAIN, s.getBytes(StandardCharsets.UTF_8));

                }
            }
            throw new RuntimeException(type);
        });
    }

    private byte[] xlsBytes(List<Map<String, Object>> records, String tableName) {
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

    private String generateCsv(List<Map<String, Object>> records, String lowerSql, PlainSelect plainSelect) {
        List<String> columnNames = getColumnNames(records, lowerSql, plainSelect);
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

    private String generateInsertSql(List<Map<String, Object>> records, String lowerSql, PlainSelect plainSelect) {
        List<String> columnNames = getColumnNames(records, lowerSql, plainSelect);
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

    private String generateUpdateSql(List<Map<String, Object>> records, String lowerSql, PlainSelect plainSelect,
                                     List<Column> primaryColumns) {
        String primaryKeyName = primaryColumns.get(0).getName();
        Map<String, Object> primaryMap = Maps.newLinkedHashMap();
        primaryMap.put(primaryKeyName, null);
        Set<String> columnNames = Sets.newHashSet(getColumnNames(records, lowerSql, plainSelect));
        return records.stream()
                .map(record -> String.format("UPDATE %s SET %s WHERE %s;",
                                getTableName(plainSelect),
                                record.entrySet().stream()
                                        .map(entry -> {
                                            String key = entry.getKey();
                                            Object value = entry.getValue();
                                            if (primaryKeyName.equals(key)) {
                                                primaryMap.put(primaryKeyName, value);
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
                                        .collect(Collectors.joining("AND"))
                        )
                )
                .collect(Collectors.joining("\r\n"));
    }

    private String entryToCondition(Map.Entry<String, Object> entry) {
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

    public List<String> getColumnNames(List<Map<String, Object>> records, String lowerSql, PlainSelect plainSelect) {
        List<String> columnNames;
        if (lowerSql.contains("*")) {
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

}
