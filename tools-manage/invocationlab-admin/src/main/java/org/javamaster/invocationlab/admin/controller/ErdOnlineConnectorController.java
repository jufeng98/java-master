package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.model.ResultVo;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineConnectorService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * @author yudong
 */
@RestController
@RequestMapping(value = "/ncnb/connector", produces = MediaType.APPLICATION_JSON_VALUE)
public class ErdOnlineConnectorController {
    @Autowired
    private ErdOnlineConnectorService erdOnlineConnectorService;

    @RequestMapping(value = "/ping", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<String> pingDb(@RequestBody JSONObject jsonObjectReq,
                                   @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineConnectorService.pingDb(jsonObjectReq, tokenVo));
    }
}
