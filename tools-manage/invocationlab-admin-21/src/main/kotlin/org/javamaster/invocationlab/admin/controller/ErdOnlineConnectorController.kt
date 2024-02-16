package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.model.ResultVo
import org.javamaster.invocationlab.admin.model.erd.TokenVo
import org.javamaster.invocationlab.admin.service.ErdOnlineConnectorService
import com.alibaba.fastjson.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

/**
 * @author yudong
 */
@RestController
@RequestMapping(value = ["/ncnb/connector"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ErdOnlineConnectorController {
    @Autowired
    private lateinit var erdOnlineConnectorService: ErdOnlineConnectorService

    @RequestMapping(value = ["/ping"], method = [RequestMethod.GET, RequestMethod.POST])
    fun pingDb(
        @RequestBody jsonObjectReq: JSONObject,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<String> {
        return ResultVo.success(erdOnlineConnectorService.pingDb(jsonObjectReq, tokenVo))
    }

}
