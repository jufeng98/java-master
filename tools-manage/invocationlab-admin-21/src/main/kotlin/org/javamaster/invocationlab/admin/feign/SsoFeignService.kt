package org.javamaster.invocationlab.admin.feign

import org.javamaster.invocationlab.admin.model.Result
import org.javamaster.invocationlab.admin.model.sso.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(value = "holder", path = "/sso-server/sso", url = "\${sso.url:}")
interface SsoFeignService {
    @PostMapping(value = ["/loginApp"])
    fun loginApp(@RequestBody reqVo: LoginLdapReqVo): Result<LoginLdapResVo>

    @PostMapping(value = ["/logout"])
    fun logoutLdap(@RequestBody reqVo: LogoutLdapReqVo): Result<String>

    @PostMapping(value = ["/getUserInfo"])
    fun getUserInfo(@RequestBody reqVo: GetUserInfoReqVo): Result<GetUserInfoResVo>
}
