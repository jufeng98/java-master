package org.javamaster.invocationlab.admin.service;

import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.Tree;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.MediaType;

import java.util.List;

/**
 * @author yudong
 */
public interface ErdOnlineQueryService {
    JSONArray load(JSONObject jsonObject) throws Exception;

    List<Tree> getQueryTreeList(String projectId) throws Exception;

    Boolean createDirOrQuery(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

    Tree queryTree(String treeNodeId) throws Exception;

    Integer deleteQueryTree(String projectId, String treeNodeId) throws Exception;

    JSONObject queryHistory(String queryId, TokenVo tokenVo);

    Tree saveQueryTree(JSONObject jsonObjectReq, String treeNodeId, TokenVo tokenVo) throws Exception;

    JSONObject execSql(JSONObject jsonObjectReq, Boolean explain, TokenVo tokenVo) throws Exception;

    List<JSONObject> execUpdate(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

    List<String> getDbs(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

    List<String> getTableNames(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

    List<Column> getTableColumns(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

    Triple<String, MediaType, byte[]> exportSql(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

}
