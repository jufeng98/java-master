package org.javamaster.invocationlab.admin.service.impl

import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.consts.ErdConst
import org.javamaster.invocationlab.admin.consts.ErdConst.ADMIN_CODE
import org.javamaster.invocationlab.admin.consts.ErdConst.COOKIE_TOKEN
import org.javamaster.invocationlab.admin.feign.SsoFeignService
import org.javamaster.invocationlab.admin.model.Result
import org.javamaster.invocationlab.admin.model.erd.TokenVo
import org.javamaster.invocationlab.admin.model.sso.GetUserInfoReqVo
import org.javamaster.invocationlab.admin.model.sso.GetUserInfoResVo
import org.javamaster.invocationlab.admin.model.sso.LoginLdapReqVo
import org.javamaster.invocationlab.admin.model.sso.LoginLdapResVo
import org.javamaster.invocationlab.admin.service.ErdOnlineUserService
import org.javamaster.invocationlab.admin.util.CookieUtils
import org.javamaster.invocationlab.admin.util.SpringUtils
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.Sets
import jakarta.servlet.http.Cookie
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.util.CollectionUtils
import org.springframework.util.DigestUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author yudong
 */
@Suppress("VulnerableCodeUsages")
@Service

class ErdOnlineUserServiceImpl : ErdOnlineUserService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var stringRedisTemplate: StringRedisTemplate

    @Autowired
    private lateinit var redisTemplateJackson: RedisTemplate<String, Any>

    @Autowired
    private lateinit var ssoFeignService: SsoFeignService

    @Value("\${sso.token.expire.timeout}")
    private var ssoTokenTimeout = 0

    override fun findUserName(code: String): String {
        val getUserInfoResVo: GetUserInfoResVo = findUserInfo(code)
        if (StringUtils.isBlank(getUserInfoResVo.account)) {
            throw ErdException("工号不存在")
        }
        return getUserInfoResVo.realName!!
    }

    fun findUserInfo(code: String): GetUserInfoResVo {
        val reqVo = GetUserInfoReqVo()
        reqVo.account = code
        reqVo.appType = "portal"
        reqVo.accountType = "0"
        val result: Result<GetUserInfoResVo> = ssoFeignService.getUserInfo(reqVo)
        if (!result.isSuccess!!) {
            throw ErdException(result.responseMsg!!)
        }
        return result.data!!
    }

    override fun findUsers(name: String, code: String): List<Map<String, Any>> {
        throw UnsupportedOperationException()
    }

    override fun login(userId: String, password: String): TokenVo {
        if (SpringUtils.proEnv) {
            var members: MutableSet<String> = stringRedisTemplate.opsForSet().members(ErdConst.ANGEL_PRO_ALLOW)!!
            if (CollectionUtils.isEmpty(members)) {
                members = Sets.newHashSet(ADMIN_CODE)
                stringRedisTemplate.opsForSet().add(ErdConst.ANGEL_PRO_ALLOW, ADMIN_CODE)
            }
            if (!members.contains(userId)) {
                throw ErdException("无权登录")
            }
        }
        val loginLdapReqVo = LoginLdapReqVo()
        loginLdapReqVo.account = userId
        loginLdapReqVo.password = DigestUtils.md5DigestAsHex(password.toByteArray(StandardCharsets.UTF_8)).lowercase(
            Locale.getDefault()
        )
        loginLdapReqVo.appType = "moonAngel"
        loginLdapReqVo.clientType = "pc"
        loginLdapReqVo.accountType = 0
        val result: Result<LoginLdapResVo> = ssoFeignService.loginApp(loginLdapReqVo)
        if (!result.isSuccess!!) {
            log.error("login error:{},{}", userId, JSONObject.toJSONString(result))
            throw ErdException(result.responseMsg!!)
        }
        val ldapResVo: LoginLdapResVo = result.data!!
        if (StringUtils.isBlank(ldapResVo.realName)) {
            val userInfo: GetUserInfoResVo = findUserInfo(userId)
            ldapResVo.realName = userInfo.realName
            ldapResVo.email = userInfo.email
            ldapResVo.mobileNo = userInfo.mobileNo
        }
        val tokenVo = TokenVo()
        val token: String = COOKIE_TOKEN + "-" + userId + "-" + RandomStringUtils.randomAlphanumeric(16)
        tokenVo.tokenType = "Bearer"
        tokenVo.accessToken = token
        tokenVo.refreshToken = ldapResVo.refreshToken
        tokenVo.expiresIn = ssoTokenTimeout
        tokenVo.scope = "select"
        tokenVo.tenantId = "0"
        tokenVo.license = "made by bm"
        tokenVo.deptId = ""
        tokenVo.deptName = ""
        tokenVo.userId = userId
        tokenVo.username = ldapResVo.realName
        tokenVo.email = ldapResVo.email
        tokenVo.mobileNo = ldapResVo.mobileNo
        if (SpringUtils.proEnv) {
            tokenVo.env = "pro"
        } else {
            tokenVo.env = "test"
        }
        redisTemplateJackson.opsForValue().set(token, tokenVo)
        refreshToken(token)
        return tokenVo
    }

    override fun refreshToken(token: String) {
        redisTemplateJackson.expire(token, ssoTokenTimeout.toLong(), TimeUnit.SECONDS)
        val cookie = Cookie(COOKIE_TOKEN, token)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.maxAge = ssoTokenTimeout
        val requestAttributes: ServletRequestAttributes =
            RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        requestAttributes.response!!.addCookie(cookie)
    }

    override fun logout(): String {
        val token: String = CookieUtils.getCookieValue(COOKIE_TOKEN)
        redisTemplateJackson.delete(token)
        return "登出成功"
    }

    override fun changePwd(newPwd: String, tokenVo: TokenVo): String {
        throw UnsupportedOperationException()
    }
}
