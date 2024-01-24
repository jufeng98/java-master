package org.javamaster.invocationlab.admin.feign;

import org.javamaster.invocationlab.admin.model.Result;
import org.javamaster.invocationlab.admin.model.sso.GetUserInfoReqVo;
import org.javamaster.invocationlab.admin.model.sso.GetUserInfoResVo;
import org.javamaster.invocationlab.admin.model.sso.LoginLdapReqVo;
import org.javamaster.invocationlab.admin.model.sso.LoginLdapResVo;
import org.javamaster.invocationlab.admin.model.sso.LogoutLdapReqVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "holder",
        path = "/sso-server/sso",
        url = "${sso.url:}")
public interface SsoFeignService {

    @PostMapping(value = "/loginApp")
    Result<LoginLdapResVo> loginApp(@RequestBody LoginLdapReqVo reqVo);

    @PostMapping(value = "/logout")
    Result<String> logoutLdap(@RequestBody LogoutLdapReqVo reqVo);

    @PostMapping(value = "/getUserInfo")
    Result<GetUserInfoResVo> getUserInfo(@RequestBody GetUserInfoReqVo reqVo);
}
