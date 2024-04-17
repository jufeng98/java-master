package org.javamaster.invocationlab.admin.service.impl;

import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.DbService;
import org.javamaster.invocationlab.admin.service.ErdOnlineConnectorService;
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService;
import org.javamaster.invocationlab.admin.util.DbUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author yudong
 */
@Service
public class ErdOnlineConnectorServiceImpl implements ErdOnlineConnectorService {
    @Autowired
    private ErdOnlineProjectService erdOnlineProjectService;
    @Autowired
    private Map<String, DbService> dbServiceMap;

    @Override
    public String pingDb(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        ErdOnlineModel erdOnlineModel = erdOnlineProjectService.getProjectDetail(projectId, tokenVo);
        DbsBean dbsBean = DbUtils.getDefaultDb(erdOnlineModel);
        DbService dbService = dbServiceMap.get(dbsBean.getSelect());
        dbService.checkDb(dbsBean);
        return "连接成功:" + dbsBean.getProperties().getUrl();
    }
}
