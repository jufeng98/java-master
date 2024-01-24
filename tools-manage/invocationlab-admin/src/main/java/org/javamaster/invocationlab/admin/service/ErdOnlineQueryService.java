package org.javamaster.invocationlab.admin.service;

import org.javamaster.invocationlab.admin.model.erd.AesReqVo;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.CommonErdVo;
import org.javamaster.invocationlab.admin.model.erd.QueryReqVo;
import org.javamaster.invocationlab.admin.model.erd.SaveQueryReqVo;
import org.javamaster.invocationlab.admin.model.erd.SqlExecResVo;
import org.javamaster.invocationlab.admin.model.erd.Table;
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
    JSONArray load(String projectId) throws Exception;

    List<Tree> getQueryTreeList(String projectId) throws Exception;

    Boolean createDirOrQuery(QueryReqVo reqVo, TokenVo tokenVo) throws Exception;

    Tree queryTree(String treeNodeId) throws Exception;

    Integer deleteQueryTree(String projectId, String treeNodeId) throws Exception;

    JSONObject queryHistory(CommonErdVo reqVo, TokenVo tokenVo);

    Tree saveQueryTree(SaveQueryReqVo reqVo, TokenVo tokenVo) throws Exception;

    SqlExecResVo execSql(CommonErdVo jsonObjectReq, TokenVo tokenVo) throws Exception;

    List<SqlExecResVo> execUpdate(CommonErdVo jsonObjectReq, TokenVo tokenVo) throws Exception;

    List<String> getDbs(String projectId, TokenVo tokenVo) throws Exception;

    List<Table> getTables(CommonErdVo reqVo, TokenVo tokenVo) throws Exception;

    List<Column> getTableColumns(CommonErdVo reqVo, TokenVo tokenVo) throws Exception;

    Triple<String, MediaType, byte[]> exportSql(CommonErdVo jsonObjectReq, TokenVo tokenVo) throws Exception;

    String aes(AesReqVo reqVo, TokenVo tokenVo) throws Exception;

    Integer getTableRecordTotal(CommonErdVo reqVo, TokenVo tokenVo) throws Exception;
}
