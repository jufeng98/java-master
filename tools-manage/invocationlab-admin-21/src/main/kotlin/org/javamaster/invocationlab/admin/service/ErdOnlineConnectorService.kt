package org.javamaster.invocationlab.admin.service

import org.javamaster.invocationlab.admin.model.erd.TokenVo
import com.alibaba.fastjson.JSONObject

/**
 * @author yudong
 */
interface ErdOnlineConnectorService {
    fun pingDb(jsonObjectReq: JSONObject, tokenVo: TokenVo): String
}
