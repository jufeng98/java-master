package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.model.ResultVo
import org.javamaster.invocationlab.admin.model.erd.TokenVo
import org.javamaster.invocationlab.admin.service.ErdOnlineUserService
import com.alibaba.fastjson.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

/**
 * @author yudong
 */
@RestController
@RequestMapping(value = ["/auth/oauth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ErdOnlineUserController {
    @Autowired
    private lateinit var erdOnlineUserService: ErdOnlineUserService

    @RequestMapping(value = ["/token"], method = [RequestMethod.GET, RequestMethod.POST])
    fun login(username: String, password: String): ResultVo<TokenVo> {
        return ResultVo.success(erdOnlineUserService.login(username, password))
    }

    @RequestMapping(value = ["/logout"], method = [RequestMethod.GET, RequestMethod.POST])
    fun logout(): ResultVo<String> {
        return ResultVo.success(erdOnlineUserService.logout())
    }

    @RequestMapping(value = ["/getUserInfo"], method = [RequestMethod.GET])
    fun getUserInfo(account: String): ResultVo<String> {
        return ResultVo.success(erdOnlineUserService.findUserName(account))
    }

    @RequestMapping(value = ["/changePwd"], method = [RequestMethod.POST])
    fun changePwd(
        @RequestBody jsonObject: JSONObject,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<String> {
        val newPwd = jsonObject.getString("newPwd")
        return ResultVo.success(erdOnlineUserService.changePwd(newPwd, tokenVo))
    }
}
