package org.javamaster.invocationlab.admin.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.enums.CipherTypeEnum;
import org.javamaster.invocationlab.admin.model.erd.AesReqVo;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.CommonErdVo;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.QueryReqVo;
import org.javamaster.invocationlab.admin.model.erd.SaveQueryReqVo;
import org.javamaster.invocationlab.admin.model.erd.SqlExecResVo;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.Tree;
import org.javamaster.invocationlab.admin.service.DbService;
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService;
import org.javamaster.invocationlab.admin.service.ErdOnlineQueryService;
import org.javamaster.invocationlab.admin.util.AesUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX;
import static org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_QUERY_TREE;
import static org.javamaster.invocationlab.admin.consts.ErdConst.QUERY_TREE_TREE_NODE;

/**
 * @author yudong
 */
@SuppressWarnings("VulnerableCodeUsages")
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
    @Autowired
    private Map<String, DbService> dbServiceMap;

    public static final String INDEX = "index";
    public static final String KEY = "key";

    @Override
    public JSONArray load(String projectId) {
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
    public Boolean createDirOrQuery(QueryReqVo reqVo, TokenVo tokenVo) throws Exception {
        String projectId = reqVo.getProjectId();
        List<Tree> trees = getQueryTreeList(projectId);
        Tree tree = new Tree();
        tree.setId(RandomUtils.nextLong(10000000000L, 99999999999L) + "");
        tree.setKey(tree.getId());
        tree.setValue(tree.getId());
        String title = reqVo.getTitle();
        tree.setTitle(title);
        tree.setLabel(title);
        tree.setIsLeaf(reqVo.getIsLeaf());
        tree.setChildren(Lists.newArrayList());
        String parentId = reqVo.getParentId();
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
    public List<String> getDbs(String projectId, TokenVo tokenVo) throws Exception {
        DbsBean dbsBean = erdProjectService.getDefaultDb(projectId, tokenVo);
        DbService dbService = dbServiceMap.get(dbsBean.getSelect());
        return dbService.getDbNames(dbsBean);
    }

    @Override
    public List<Table> getTables(CommonErdVo reqVo, TokenVo tokenVo) throws Exception {
        String projectId = reqVo.getProjectId();
        String selectDB = reqVo.getSelectDB();

        DbsBean dbsBean = erdProjectService.getDefaultDb(projectId, tokenVo);
        DbService dbService = dbServiceMap.get(dbsBean.getSelect());

        return dbService.getTables(dbsBean, selectDB);
    }

    @Override
    public List<Column> getTableColumns(CommonErdVo reqVo, TokenVo tokenVo) throws Exception {
        String projectId = reqVo.getProjectId();
        String selectDB = reqVo.getSelectDB();
        String tableName = reqVo.getTableName();

        DbsBean dbsBean = erdProjectService.getDefaultDb(projectId, tokenVo);
        DbService dbService = dbServiceMap.get(dbsBean.getSelect());

        return dbService.getTableColumns(dbsBean, selectDB, tableName);
    }

    @Override
    public JSONObject queryHistory(CommonErdVo reqVo, TokenVo tokenVo) {
        String key = ERD_PREFIX + "sqlHistory:" + reqVo.getQueryId();
        Long size = redisTemplateJackson.opsForList().size(key);
        int start = (reqVo.getPage() - 1) * reqVo.getPageSize();
        int end = start + reqVo.getPageSize() - 1;
        List<Object> range = redisTemplateJackson.opsForList().range(key, start, end);
        JSONObject tableData = new JSONObject();
        tableData.put("records", range);
        tableData.put("total", size);
        return tableData;
    }

    @Override
    public Tree saveQueryTree(SaveQueryReqVo reqVo, TokenVo tokenVo) throws Exception {
        String treeNodeId = reqVo.getTreeNodeId();
        String jsonStrData = (String) stringRedisTemplate.opsForHash().get(QUERY_TREE_TREE_NODE, treeNodeId);
        Tree tree = objectMapper.readValue(jsonStrData, Tree.class);

        String projectId = reqVo.getProjectId();
        String sqlInfo = reqVo.getSqlInfo();
        if (StringUtils.isNotBlank(sqlInfo)) {
            tree.setSqlInfo(sqlInfo);
            tree.setSelectDB(reqVo.getSelectDB());
        }
        String title = reqVo.getTitle();
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

    @Override
    public SqlExecResVo execSql(CommonErdVo reqVo, TokenVo tokenVo) throws Exception {
        String projectId = reqVo.getProjectId();

        DbsBean dbsBean = erdProjectService.getDefaultDb(projectId, tokenVo);
        DbService dbService = dbServiceMap.get(dbsBean.getSelect());

        return dbService.execSql(reqVo, dbsBean, tokenVo);
    }

    @Override
    public Integer getTableRecordTotal(CommonErdVo reqVo, TokenVo tokenVo) throws Exception {
        String projectId = reqVo.getProjectId();

        DbsBean dbsBean = erdProjectService.getDefaultDb(projectId, tokenVo);
        DbService dbService = dbServiceMap.get(dbsBean.getSelect());

        return dbService.getTableRecordTotal(reqVo, dbsBean, tokenVo);
    }

    @Override
    public List<SqlExecResVo> execUpdate(CommonErdVo reqVo, TokenVo tokenVo) throws Exception {
        String projectId = reqVo.getProjectId();

        DbsBean dbsBean = erdProjectService.getDefaultDb(projectId, tokenVo);
        DbService dbService = dbServiceMap.get(dbsBean.getSelect());
        return dbService.execUpdate(dbsBean, tokenVo, reqVo);
    }

    @Override
    public Triple<String, MediaType, byte[]> exportSql(CommonErdVo reqVo, TokenVo tokenVo) throws Exception {
        String projectId = reqVo.getProjectId();

        DbsBean dbsBean = erdProjectService.getDefaultDb(projectId, tokenVo);
        DbService dbService = dbServiceMap.get(dbsBean.getSelect());

        return dbService.exportSql(dbsBean, tokenVo, reqVo);
    }

    @Override
    public String aes(AesReqVo reqVo, TokenVo tokenVo) throws Exception {
        DbsBean dbsBean = erdProjectService.getDefaultDb(reqVo.getProjectId(), tokenVo);

        String cipherType = dbsBean.getProperties().getCipherType();
        String cipherKey = dbsBean.getProperties().getCipherKey();
        if (StringUtils.isBlank(cipherType) || StringUtils.isBlank(cipherKey)) {
            throw new ErdException("请先配置密钥信息");
        }

        CipherTypeEnum cipherTypeEnum = CipherTypeEnum.getByType(cipherType);

        String value = reqVo.getValue();
        if ("decrypt".equals(reqVo.getOpType())) {
            return AesUtils.decrypt(value, cipherTypeEnum, cipherKey);
        } else {
            return AesUtils.encrypt(value, cipherTypeEnum, cipherKey);
        }
    }

}
