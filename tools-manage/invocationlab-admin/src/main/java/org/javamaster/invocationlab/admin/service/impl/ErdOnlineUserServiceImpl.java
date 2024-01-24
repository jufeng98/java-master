package org.javamaster.invocationlab.admin.service.impl;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.consts.ErdConst;
import org.javamaster.invocationlab.admin.feign.SsoFeignService;
import org.javamaster.invocationlab.admin.model.Result;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.sso.GetUserInfoReqVo;
import org.javamaster.invocationlab.admin.model.sso.GetUserInfoResVo;
import org.javamaster.invocationlab.admin.model.sso.LoginLdapResVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineUserService;
import org.javamaster.invocationlab.admin.util.CookieUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ADMIN_CODE;
import static org.javamaster.invocationlab.admin.consts.ErdConst.COOKIE_TOKEN;

/**
 * @author yudong
 */
@Service
@Slf4j
public class ErdOnlineUserServiceImpl implements ErdOnlineUserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplateJackson;
    @Autowired
    private SsoFeignService ssoFeignService;
    @Value("${sso.token.expire.timeout}")
    private int ssoTokenTimeout;

    @Override
    public String findUserName(String code) {
        GetUserInfoResVo getUserInfoResVo = findUserInfo(code);
        if (StringUtils.isBlank(getUserInfoResVo.getAccount())) {
            throw new ErdException("不存在:" + code);
        }
        return getUserInfoResVo.getRealName();
    }

    public GetUserInfoResVo findUserInfo(String code) {
        GetUserInfoReqVo reqVo = new GetUserInfoReqVo();
        reqVo.setAccount(code);
        reqVo.setAppType("portal");
        reqVo.setAccountType("0");
        Result<GetUserInfoResVo> result = ssoFeignService.getUserInfo(reqVo);
        if (!result.getIsSuccess()) {
            throw new ErdException(result.getResponseMsg());
        }
        return result.getData();
    }

    @Override
    public List<Map<String, Object>> findUsers(String name, String code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TokenVo login(String userId, String password) {
        if (SpringUtils.isProEnv()) {
            Set<String> members = stringRedisTemplate.opsForSet().members(ErdConst.ANGEL_PRO_ALLOW);
            if (CollectionUtils.isEmpty(members)) {
                members = Sets.newHashSet(ADMIN_CODE);
                stringRedisTemplate.opsForSet().add(ErdConst.ANGEL_PRO_ALLOW, ADMIN_CODE);
            }
            if (!members.contains(userId)) {
                throw new ErdException("无权登录");
            }
        }

//        LoginLdapReqVo loginLdapReqVo = new LoginLdapReqVo();
//        loginLdapReqVo.setAccount(userId);
//        loginLdapReqVo.setPassword(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)).toLowerCase());
//        loginLdapReqVo.setAppType("moonAngel");
//        loginLdapReqVo.setClientType("pc");
//        loginLdapReqVo.setAccountType(0);
//        Result<LoginLdapResVo> result = ssoFeignService.loginApp(loginLdapReqVo);
//        if (!result.getIsSuccess()) {
//            log.error("login error:{},{}", userId, JSONObject.toJSONString(result));
//            throw new ErdException(result.getResponseMsg());
//        }
//        LoginLdapResVo ldapResVo = result.getData();

        LoginLdapResVo ldapResVo = new LoginLdapResVo();
        ldapResVo.setRefreshToken(org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(10));
        ldapResVo.setEmail("123456@qq.com");
        ldapResVo.setAccount(ADMIN_CODE);
        ldapResVo.setRealName("admin");
        ldapResVo.setToken(org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(10));
        ldapResVo.setMobileNo("13800138000");

        if (StringUtils.isBlank(ldapResVo.getRealName())) {
            GetUserInfoResVo userInfo = findUserInfo(userId);
            ldapResVo.setRealName(userInfo.getRealName());
            ldapResVo.setEmail(userInfo.getEmail());
            ldapResVo.setMobileNo(userInfo.getMobileNo());
        }
        TokenVo tokenVo = new TokenVo();
        String token = COOKIE_TOKEN + "-" + userId + "-" + RandomStringUtils.randomAlphanumeric(16);
        tokenVo.setTokenType("Bearer");
        tokenVo.setAccessToken(token);
        tokenVo.setRefreshToken(ldapResVo.getRefreshToken());
        tokenVo.setExpiresIn(ssoTokenTimeout);
        tokenVo.setScope("select");
        tokenVo.setTenantId("0");
        tokenVo.setLicense("made by erd");
        tokenVo.setDeptId("");
        tokenVo.setDeptName("");
        tokenVo.setUserId(userId);
        tokenVo.setUsername(ldapResVo.getRealName());
        tokenVo.setEmail(ldapResVo.getEmail());
        tokenVo.setMobileNo(ldapResVo.getMobileNo());
        if (SpringUtils.isProEnv()) {
            tokenVo.setEnv("pro");
        } else {
            tokenVo.setEnv("test");
        }
        redisTemplateJackson.opsForValue().set(token, tokenVo);
        refreshToken(token);
        return tokenVo;
    }

    @Override
    public void refreshToken(String token) {
        redisTemplateJackson.expire(token, ssoTokenTimeout, TimeUnit.SECONDS);
        Cookie cookie = new Cookie(COOKIE_TOKEN, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(ssoTokenTimeout);
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //noinspection ConstantConditions
        requestAttributes.getResponse().addCookie(cookie);
    }

    @Override
    public String logout() {
        String token = CookieUtils.getCookieValue(COOKIE_TOKEN);
        redisTemplateJackson.delete(token);
        return "登出成功";
    }

    @Override
    public String changePwd(String newPwd, TokenVo tokenVo) {
        throw new UnsupportedOperationException();
    }
}
