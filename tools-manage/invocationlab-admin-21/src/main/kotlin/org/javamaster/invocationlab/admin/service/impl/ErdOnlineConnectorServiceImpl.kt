package org.javamaster.invocationlab.admin.service.impl

import org.javamaster.invocationlab.admin.model.erd.TokenVo
import org.javamaster.invocationlab.admin.service.DbService
import org.javamaster.invocationlab.admin.service.ErdOnlineConnectorService
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService
import org.javamaster.invocationlab.admin.util.DbUtils.getDefaultDb
import com.alibaba.fastjson.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author yudong
 */
@Service
class ErdOnlineConnectorServiceImpl : ErdOnlineConnectorService {
    @Autowired
    private lateinit var erdOnlineProjectService: ErdOnlineProjectService


    override fun pingDb(jsonObjectReq: JSONObject, tokenVo: TokenVo): String {
        val projectId = jsonObjectReq.getString("projectId")
        val erdOnlineModel = erdOnlineProjectService.getProjectDetail(projectId, tokenVo)
        val dbsBean = getDefaultDb(erdOnlineModel)
        val dbService = DbService.getInstance(dbsBean.select!!)
        dbService.checkDb(dbsBean)
        return "连接成功:" + dbsBean.properties!!.url
    }
}
