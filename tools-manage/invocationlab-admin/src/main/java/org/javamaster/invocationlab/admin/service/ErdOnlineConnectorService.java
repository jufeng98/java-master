package org.javamaster.invocationlab.admin.service;

import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import com.alibaba.fastjson.JSONObject;

/**
 * @author yudong
 */
public interface ErdOnlineConnectorService {
    String pingDb(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;
}
