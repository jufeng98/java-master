package org.javamaster.invocationlab.admin.service.impl;

import com.alibaba.druid.DbType;
import com.alibaba.fastjson.JSONObject;
import com.dbschema.mongo.MongoConnection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.consts.ErdConst;
import org.javamaster.invocationlab.admin.enums.SqlTypeEnum;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.CommonErdVo;
import org.javamaster.invocationlab.admin.model.erd.DatatypeBean;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.EntitiesBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.ExecuteHistoryBean;
import org.javamaster.invocationlab.admin.model.erd.FieldsBean;
import org.javamaster.invocationlab.admin.model.erd.IndexsBean;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.SqlExecResVo;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.javamaster.invocationlab.admin.model.erd.TableData;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.MongoDbService;
import org.javamaster.invocationlab.admin.util.ErdUtils;
import org.javamaster.invocationlab.admin.util.ExecutorUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.javamaster.invocationlab.admin.util.MongoUtils;
import org.javamaster.invocationlab.admin.util.SessionUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX;
import static org.javamaster.invocationlab.admin.consts.ErdConst.QUERY_RES;
import static org.javamaster.invocationlab.admin.util.DbUtils.getDefaultDb;
import static org.javamaster.invocationlab.admin.util.JsonUtils.STANDARD_PATTERN;


/**
 * @author yudong
 */
@Slf4j
@Service("MongoDB")
public class MongoDbServiceImpl implements MongoDbService, DisposableBean {

    @Override
    public void checkDb(DbsBean dbsBean) {
        try {
            String version = MongoUtils.executeMongo(dbsBean, connection -> connection.getService().getVersion());
            log.info("连接成功:{}", version);
        } catch (Exception e) {
            log.error("连接失败", e);
            throw new ErdException(e.getMessage());
        }
    }

    @Override
    public List<String> getDbNames(DbsBean dbsBean) {
        return MongoUtils.executeMongo(dbsBean, connection -> {
            List<MongoDatabase> databases = connection.getService().getDatabases();
            return databases.stream()
                    .map(MongoDatabase::getName)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<Table> getTables(DbsBean dbsBean, String selectDB) {
        return MongoUtils.executeMongo(dbsBean, connection -> {
            String dbName = MongoUtils.resolveUrlDbName(dbsBean.getProperties().getUrl());
            return connection.getService().getCollectionNames(dbName).stream()
                    .map(it -> Table.builder().name(it).remarks("Collection").build())
                    .collect(Collectors.toList());
        });
    }

    @Override
    public DbType getDbType() {
        return DbType.other;
    }

    @Override
    public List<SqlExecResVo> execUpdate(DbsBean dbsBean, TokenVo tokenVo, CommonErdVo reqVo) {
        return MongoUtils.executeMongo(dbsBean, connection -> reqVo.getRows().stream()
                .map(reqRow -> execUpdate(connection, reqRow, tokenVo, reqVo.getQueryId()))
                .collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    private SqlExecResVo execUpdate(MongoConnection connection, Object reqRow, TokenVo tokenVo, String queryId) {
        @SuppressWarnings("unchecked")
        JSONObject row = AbstractDbService.removeNullColumns((LinkedHashMap<String, Object>) reqRow);
        SqlTypeEnum sqlTypeEnum = SqlTypeEnum.getByType(row.getString(ErdConst.ROW_OPERATION_TYPE));
        String primaryKeyValue = (String) row.get(ErdConst.MONGO_KEY_NAME);

        Document rowDoc = Document.parse(row.toJSONString());

        delKeys(rowDoc);

        if (sqlTypeEnum == SqlTypeEnum.UPDATE && rowDoc.isEmpty()) {
            throw new ErdException("数据无变化,未做任何更新操作!");
        }

        String collectionName = SessionUtils.getFromSession(ErdConst.EXEC_COLLECTION_NAME);

        long start = System.currentTimeMillis();
        int num = MongoUtils.executeMongo(collectionName, connection, collection -> {
            switch (sqlTypeEnum) {
                case INSERT:
                    log.info("{}-{} execute insert action:\n{}", tokenVo.getUserId(), tokenVo.getUsername(), rowDoc);

                    collection.insertOne(rowDoc);
                    return 1;
                case UPDATE:
                    Document filter = new Document().append(ErdConst.MONGO_KEY_NAME, new ObjectId(primaryKeyValue));
                    Document updated = new Document().append("$set", rowDoc);
                    log.info("{}-{} execute update action:\nfilter:{}\r\nupdated:{}", tokenVo.getUserId(), tokenVo.getUsername(),
                            filter, updated);

                    UpdateResult updateResult = collection.updateOne(filter, updated);
                    return (int) updateResult.getModifiedCount();
                case DELETE:
                    Document filterDel = new Document().append(ErdConst.MONGO_KEY_NAME, new ObjectId(primaryKeyValue));
                    log.info("{}-{} execute del action:\n{}", tokenVo.getUserId(), tokenVo.getUsername(), filterDel);

                    DeleteResult deleteResult = collection.deleteOne(filterDel);
                    return (int) deleteResult.getDeletedCount();
                default:
                    throw new ErdException("参数有误:" + sqlTypeEnum);
            }
        });
        long cost = System.currentTimeMillis() - start;
        log.info("execute dml sql res num:{},cost:{}ms", num, cost);

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

        addSqlToExecuteHistory(rowDoc.toJson(), connection.getUrl(), tokenVo, queryId, cost);
        return resVo;
    }

    private Document getOldQueryDocument(Object rowUniqueKey) {
        List<Map<String, Object>> oldRecords = SessionUtils.getFromSession(QUERY_RES);
        if (oldRecords == null) {
            throw new ErdException("请重新刷新查询后再重试");
        }
        List<Map<String, Object>> list = oldRecords.stream()
                .filter(oldRecord -> oldRecord.get(ErdConst.ERD_ROW_KEY).equals(rowUniqueKey))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            // 新增记录的情况
            return null;
        }
        Document oldRow = (Document) list.get(0);
        return new Document(oldRow);
    }

    private void delKeys(Document row) {
        Document oldDoc = getOldQueryDocument(row.get(ErdConst.ERD_ROW_KEY));

        List<String> delKeys = row.entrySet().stream()
                .filter(entry -> {
                    boolean isNull = Objects.equals(entry.getValue(), ErdConst.NULL_VALUE);
                    if (isNull) {
                        // 过滤到值为null的
                        return true;
                    }
                    if (oldDoc != null) {
                        // 过滤掉值没有变化的
                        boolean isEquals = Objects.equals(oldDoc.get(entry.getKey()), entry.getValue());
                        if (isEquals) {
                            return true;
                        }
                    }
                    return ErdConst.ROW_OPERATION_TYPE.equals(entry.getKey())
                            || ErdConst.ERD_ROW_KEY.equals(entry.getKey())
                            || ErdConst.MONGO_KEY_NAME.equals(entry.getKey())
                            || ErdConst.INDEX.equals(entry.getKey());
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        for (String delKey : delKeys) {
            row.remove(delKey);
        }
    }

    @Override
    public List<Column> getTableColumns(DbsBean dbsBean, String selectDB, String tableName) {
        return MongoUtils.executeMongo(dbsBean, connection -> {
            MongoCollection<Document> collection = connection.getService().getDatabase(selectDB).getCollection(tableName);
            Document first = collection.find().first();
            if (first == null) {
                return Collections.emptyList();
            }
            return first.entrySet().stream()
                    .map(entry -> {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        boolean primary = key.equals(ErdConst.MONGO_KEY_NAME);
                        Column column = new Column();
                        column.setName(key);
                        if (value != null) {
                            Class<?> aClass = value.getClass();
                            column.setTypeName(aClass.getSimpleName().toUpperCase());
                        } else {
                            column.setTypeName(String.class.getSimpleName().toUpperCase());
                        }
                        column.setDigits(0);
                        column.setPrecRadix(10);
                        column.setNullable(primary ? 0 : 1);
                        column.setRemarks("");
                        column.setCharOctetLength(0);
                        column.setIsNullable(primary ? "NO" : "YES");
                        column.setIsAutoincrement("NO");
                        column.setPrimaryKey(primary);
                        return column;
                    })
                    .collect(Collectors.toList());
        });
    }

    @Override
    public SqlExecResVo execSql(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo) {
        return MongoUtils.executeMongo(dbsBean, connection -> {
            if (reqVo.getExplain()) {
                return execExplainSql(connection, reqVo, tokenVo);
            }

            Pair<SqlTypeEnum, String> pair = checkSqlType(reqVo.getSql());
            if (pair.getLeft() == SqlTypeEnum.SELECT
                    || pair.getLeft() == SqlTypeEnum.UNKNOWN) {
                return execDqlSql(connection, reqVo, tokenVo, pair.getLeft(), pair.getRight());
            } else if (pair.getLeft() == SqlTypeEnum.INSERT
                    || pair.getLeft() == SqlTypeEnum.UPDATE
                    || pair.getLeft() == SqlTypeEnum.DROP) {
                return execDmlOrDdlSql(connection, reqVo, tokenVo);
            }
            throw new UnsupportedOperationException();
        });
    }

    private SqlExecResVo execExplainSql(MongoConnection connection, CommonErdVo reqVo, TokenVo tokenVo) throws Exception {
        reqVo.setSql(reqVo.getSql() + ".explain('executionStats')");

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setTableColumns(Collections.emptyMap());
        resVo.setQueryKey(ErdConst.COUNTER.incrementAndGet());
        resVo.setPrimaryKeys(Collections.emptyList());
        resVo.setPage(reqVo.getPage());
        resVo.setPageSize(reqVo.getPageSize());

        TableData tableData = new TableData();
        resVo.setShowPagination(false);
        resVo.setTableData(tableData);

        List<Map<String, Object>> records = Lists.newArrayList();
        tableData.setRecords(records);

        long start = System.currentTimeMillis();
        long cost;
        boolean first = true;
        log.info("{}-{} execute explain sql:\n{}", tokenVo.getUserId(), tokenVo.getUsername(), reqVo.getSql());
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(reqVo.getSql())) {
            cost = System.currentTimeMillis() - start;
            while (resultSet.next()) {
                Document document = (Document) resultSet.getObject(1);
                if (first) {
                    first = false;
                    resVo.setColumns(document.keySet());
                }
                records.add(document);
            }
        }
        log.info("{}-{} execute explain sql res num:{}", tokenVo.getUserId(), tokenVo.getUsername(), records.size());

        addSqlToExecuteHistory(reqVo.getSql(), connection.getUrl(), tokenVo, reqVo.getQueryId(), cost);

        return resVo;
    }

    private SqlExecResVo execDqlSql(MongoConnection connection, CommonErdVo reqVo, TokenVo tokenVo,
                                    SqlTypeEnum sqlTypeEnum, String collectionName) throws Exception {
        Pair<String, Boolean> pair = modifyQuerySql(reqVo, sqlTypeEnum);
        String sql = pair.getLeft();

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setTableColumns(Collections.emptyMap());
        resVo.setQueryKey(ErdConst.COUNTER.incrementAndGet());
        if (sqlTypeEnum == SqlTypeEnum.SELECT) {
            resVo.setPrimaryKeys(Lists.newArrayList(ErdConst.MONGO_KEY_NAME));
        } else {
            resVo.setPrimaryKeys(Lists.newArrayList());
        }
        resVo.setPage(reqVo.getPage());
        resVo.setPageSize(reqVo.getPageSize());

        TableData tableData = new TableData();
        resVo.setShowPagination(pair.getRight());
        resVo.setTableData(tableData);

        List<Map<String, Object>> records = Lists.newArrayList();
        long start = System.currentTimeMillis();
        long cost;

        boolean first = true;
        log.info("{}-{} execute dql sql:\n{}", tokenVo.getUserId(), tokenVo.getUsername(), sql);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            cost = System.currentTimeMillis() - start;
            while (resultSet.next()) {
                Object obj = resultSet.getObject(1);
                if (obj instanceof Document) {
                    Document document = (Document) obj;
                    if (first) {
                        first = false;
                        resVo.setColumns(document.keySet());
                    }
                    records.add(document);
                } else {
                    if (first) {
                        first = false;
                        resVo.setColumns(Sets.newHashSet(sql));
                    }
                    records.add(Maps.newHashMap(Collections.singletonMap(sql, obj)));
                }
            }
        }
        log.info("{}-{} execute dql sql res size:{}", tokenVo.getUserId(), tokenVo.getUsername(), records.size());

        if (resVo.getColumns() == null) {
            throw new ErdException("collection不存在或结果集为空");
        }

        int anInt = Integer.parseInt(RandomUtils.nextInt(1000000, 9999999) + "01");
        for (int i = 0; i < records.size(); i++) {
            Map<String, Object> rowMap = records.get(i);
            rowMap.put(ErdConst.ERD_ROW_KEY, anInt + i);
        }

        if (records.isEmpty()) {
            tableData.setRealTotal(0);
        } else if (records.size() > reqVo.getPageSize()) {
            records = records.subList(0, reqVo.getPageSize());
            tableData.setTotal(reqVo.getPage() * reqVo.getPageSize() + 1);
        } else {
            tableData.setRealTotal((reqVo.getPage() - 1) * reqVo.getPageSize() + records.size());
        }
        tableData.setRecords(records);

        SessionUtils.saveToSession(QUERY_RES, records);
        SessionUtils.saveToSession(ErdConst.EXEC_COLLECTION_NAME, collectionName);

        addSqlToExecuteHistory(sql, connection.getUrl(), tokenVo, reqVo.getQueryId(), cost);

        return resVo;
    }

    private SqlExecResVo execDmlOrDdlSql(MongoConnection connection, CommonErdVo reqVo, TokenVo tokenVo) {
        List<String> sqlList = Arrays.stream(reqVo.getSql().split(";"))
                .collect(Collectors.toList());
        if (sqlList.size() > 2000) {
            throw new ErdException("一次执行sql条数不能超过2000条!");
        }

        log.info("{}-{} execute dql sql:\n{}", tokenVo.getUserId(), tokenVo.getUsername(), sqlList);
        long start = System.currentTimeMillis();
        long num = sqlList.stream()
                .mapToInt(it -> {
                    try (Statement statement = connection.createStatement()) {
                        return statement.executeUpdate(it);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .count();
        long end = System.currentTimeMillis();
        log.info("{}-{} execute dml sql res num:{}", tokenVo.getUserId(), tokenVo.getUsername(), num);

        SqlExecResVo resVo = new SqlExecResVo();
        resVo.setColumns(Sets.newHashSet("affect_num", "cost"));
        resVo.setTableColumns(Collections.emptyMap());
        resVo.setPrimaryKeys(Collections.emptyList());
        resVo.setQueryKey(ErdConst.COUNTER.incrementAndGet());

        TableData tableData = new TableData();
        tableData.setTotal(1);

        Map<String, Object> map = Maps.newHashMap();
        map.put("affect_num", num);
        long cost = end - start;
        map.put("cost", cost + "ms");

        //noinspection unchecked
        tableData.setRecords(Lists.newArrayList(map));

        resVo.setTableData(tableData);

        addSqlToExecuteHistory(reqVo.getSql(), connection.getUrl(), tokenVo, reqVo.getQueryId(), cost);
        return resVo;
    }

    /**
     * 修改查询sql
     *
     * @return Pair(修改后的SQL, 是否缺少limit)
     */
    protected Pair<String, Boolean> modifyQuerySql(CommonErdVo reqVo, SqlTypeEnum sqlTypeEnum) {
        String sql = reqVo.getSql();
        if (sqlTypeEnum == SqlTypeEnum.UNKNOWN) {
            return Pair.of(sql, false);
        }
        if (sql.contains(".limit")) {
            return Pair.of(sql, false);
        } else {
            int offset = (reqVo.getPage() - 1) * reqVo.getPageSize();
            sql = sql + ".skip(" + offset + ").limit(" + (reqVo.getPageSize() + 1) + ")";
            return Pair.of(sql, true);
        }
    }

    /**
     * 检查SQL类型
     *
     * @return Pair(SQL类型, collection名称)
     */
    private Pair<SqlTypeEnum, String> checkSqlType(String sql) {
        int idx = sql.indexOf("(");
        if (idx == -1) {
            return Pair.of(SqlTypeEnum.UNKNOWN, "");
        }
        String command = sql.substring(0, idx).replaceAll("\\s", "");
        String[] commands = command.split("\\.");
        if (commands.length != 3) {
            return Pair.of(SqlTypeEnum.UNKNOWN, "");
        }
        String name = commands[1];
        if (commands[2].startsWith(SqlTypeEnum.INSERT.type)) {
            return Pair.of(SqlTypeEnum.INSERT, name);
        } else if (commands[2].startsWith(SqlTypeEnum.UPDATE.type)) {
            return Pair.of(SqlTypeEnum.UPDATE, name);
        } else if (commands[2].startsWith("find")) {
            return Pair.of(SqlTypeEnum.SELECT, name);
        } else if (commands[2].startsWith(SqlTypeEnum.DROP.type)) {
            return Pair.of(SqlTypeEnum.DROP, name);
        } else {
            // TODO
            return Pair.of(SqlTypeEnum.UNKNOWN, "");
        }
    }

    @Override
    public Triple<String, MediaType, byte[]> exportSql(DbsBean dbsBean, TokenVo tokenVo, CommonErdVo reqVo) {
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
            String res = AbstractDbService.generateCsv(records);
            return Triple.of(fileName, MediaType.TEXT_PLAIN, res.getBytes(StandardCharsets.UTF_8));
        }

        Pair<SqlTypeEnum, String> pair = checkSqlType(sql);
        String collectionName = pair.getRight();

        if ("xls".equals(type)) {
            byte[] bytes = AbstractDbService.excelBytes(records, collectionName);
            return Triple.of(fileName, new MediaType("application", "vnd.ms-excel"), bytes);
        }

        if (pair.getLeft() != SqlTypeEnum.SELECT) {
            throw new ErdException("不支持的操作");
        }

        if ("sqlInsert".equals(type)) {
            fileName = "INSERT SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + ".js";
            String tmpSql = generateInsertSql(records, collectionName);
            return Triple.of(fileName, MediaType.TEXT_PLAIN, tmpSql.getBytes(StandardCharsets.UTF_8));
        }

        if ("sqlUpdate".equals(type)) {
            fileName = "UPDATE SQL导出-" + DateFormatUtils.format(new Date(), STANDARD_PATTERN) + ".js";
            String tmpSql = generateUpdateSql(records, collectionName);
            return Triple.of(fileName, MediaType.TEXT_PLAIN, tmpSql.getBytes(StandardCharsets.UTF_8));
        }

        return null;
    }

    private String generateUpdateSql(List<Map<String, Object>> records, String collectionName) {
        return records.stream()
                .map(record -> {
                    Document document = (Document) record;
                    ObjectId objectId = (ObjectId) document.get(ErdConst.MONGO_KEY_NAME);

                    document.remove(ErdConst.MONGO_KEY_NAME);

                    String filterJsonStr = String.format("{\"_id\":ObjectId(\"%s\")}", objectId.toHexString());
                    Document updated = new Document().append("$set", document);

                    return String.format("db.%s.updateOne(%s,%s);", collectionName, filterJsonStr, updated.toJson());
                })
                .collect(Collectors.joining("\r\n"));
    }

    private String generateInsertSql(List<Map<String, Object>> records, String collectionName) {
        return records.stream()
                .map(record -> {
                    Document document = (Document) record;
                    document.remove(ErdConst.MONGO_KEY_NAME);

                    String jsonStr = document.toJson();
                    return String.format("db.%s.insertOne(%s);", collectionName, jsonStr);
                })
                .collect(Collectors.joining("\r\n"));
    }

    @Override
    public Integer getTableRecordTotal(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo) {
        Pair<SqlTypeEnum, String> pair = checkSqlType(reqVo.getSql());
        String collectionName = pair.getRight();
        return MongoUtils.executeMongo(dbsBean, collectionName, documentMongoCollection -> {
            long count = documentMongoCollection.countDocuments();
            return (int) count;
        });
    }

    @Override
    public ModulesBean refreshModule(ErdOnlineModel erdOnlineModel, String moduleName) {
        DbsBean dbsBean = getDefaultDb(erdOnlineModel);
        ModulesBean modulesBean = ErdUtils.findModulesBean(moduleName, erdOnlineModel);
        List<DatatypeBean> datatypeBeans = erdOnlineModel.getProjectJSON().getDataTypeDomains().getDatatype();
        String dbName = MongoUtils.resolveUrlDbName(dbsBean.getProperties().getUrl());

        List<EntitiesBean> entitiesBeans = modulesBean.getEntities().stream()
                .peek(entitiesBean -> {
                    String collectionName = entitiesBean.getTitle();

                    List<Column> columns = getTableColumns(dbsBean, dbName, collectionName);
                    List<FieldsBean> fieldsBeans = ErdUtils.toFieldsBeans(columns, datatypeBeans,
                            pair -> pair.getLeft().getMongoDB().getType().equals(pair.getRight().getTypeName().toUpperCase()));

                    entitiesBean.setFields(fieldsBeans);

                    List<IndexsBean> indexesBeans = MongoUtils.listIndexes(dbsBean, collectionName);

                    entitiesBean.setIndexs(indexesBeans);

                    entitiesBean.setOriginalCreateTableSql("");
                })
                .collect(Collectors.toList());

        modulesBean.setEntities(entitiesBeans);

        return modulesBean;
    }

    protected void addSqlToExecuteHistory(String sql, String url, TokenVo tokenVo, String queryId, long duration) {
        ExecutorUtils.startAsyncTask(() -> {
            String urlDbName = MongoUtils.resolveUrlDbName(url);
            String key = ERD_PREFIX + "sqlHistory:" + queryId;
            ExecuteHistoryBean bean = new ExecuteHistoryBean();
            bean.setSqlInfo(sql);
            bean.setDbName(urlDbName);
            bean.setDuration(duration);
            bean.setCreateTime(new Date());
            bean.setCreator(tokenVo.getUserId());
            bean.setParams("");
            @SuppressWarnings("unchecked")
            RedisTemplate<String, Object> redisTemplateJackson = (RedisTemplate<String, Object>) SpringUtils.getContext()
                    .getBean("redisTemplateJackson");
            redisTemplateJackson.opsForList().leftPush(key, bean);
            Long size = redisTemplateJackson.opsForList().size(key);
            //noinspection DataFlowIssue,ConstantConditions
            if (size > 500) {
                redisTemplateJackson.opsForList().rightPop(key);
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        MongoUtils.close();
    }
}
