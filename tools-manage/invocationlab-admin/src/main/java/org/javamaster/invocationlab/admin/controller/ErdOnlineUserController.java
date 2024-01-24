package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.model.ResultVo;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineUserService;
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
@RequestMapping(value = "/auth/oauth", produces = MediaType.APPLICATION_JSON_VALUE)
public class ErdOnlineUserController {
    @Autowired
    private ErdOnlineUserService erdOnlineUserService;

    @RequestMapping(value = "/token", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<TokenVo> login(String username, String password) {
        return ResultVo.success(erdOnlineUserService.login(username, password));
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<String> logout() {
        return ResultVo.success(erdOnlineUserService.logout());
    }

    @RequestMapping(value = "/getUserInfo", method = {RequestMethod.GET})
    public ResultVo<String> getUserInfo(String account) {
        return ResultVo.success(erdOnlineUserService.findUserName(account));
    }

    @RequestMapping(value = "/changePwd", method = {RequestMethod.POST})
    public ResultVo<String> changePwd(@RequestBody JSONObject jsonObject, @SessionAttribute("tokenVo") TokenVo tokenVo) {
        String newPwd = jsonObject.getString("newPwd");
        return ResultVo.success(erdOnlineUserService.changePwd(newPwd, tokenVo));
    }
}
