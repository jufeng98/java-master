package org.javamaster.invocationlab.admin.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.UsersVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineUserService;
import org.javamaster.invocationlab.admin.util.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.javamaster.invocationlab.admin.consts.ErdConst.COOKIE_TOKEN;
import static org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX;

/**
 * @author yudong
 */
@Service
@Slf4j
public class ErdOnlineUserServiceImpl implements ErdOnlineUserService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplateJackson;

    @Override
    public String findUserName(String code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, Object>> findUsers(String name, String code) {
        throw new UnsupportedOperationException();
    }

    private UsersVo saveUserToRedis(String userId, String empName, String orgId, String orgName) {
        UsersVo usersVo = new UsersVo();
        usersVo.setId(userId);
        usersVo.setUsername(empName);
        byte[] pwdBytes = ("admin").getBytes(StandardCharsets.UTF_8);
        usersVo.setPwd(DigestUtils.md5DigestAsHex(pwdBytes));
        usersVo.setDeptId(orgId);
        usersVo.setDeptName(orgName);
        usersVo.setCreateTime(new Date());
        redisTemplateJackson.opsForHash().put(ERD_PREFIX + ":user", userId, usersVo);
        return usersVo;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public TokenVo login(String userId, String password) {
        if (!userId.equals("admin")) {
            throw new ErdException("用户名错误");
        }
        String orgName = "develop apartment one";
        String orgId = "10000001";
        UsersVo usersVo = (UsersVo) redisTemplateJackson.opsForHash().get(ERD_PREFIX + ":user", userId);
        if (usersVo == null) {
            usersVo = saveUserToRedis(userId, userId, orgId, orgName);
        }
        if (!usersVo.getPwd().equals(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)))) {
            throw new ErdException("密码错误");
        }
        TokenVo tokenVo = new TokenVo();
        String token = COOKIE_TOKEN + "-" + userId + "-" + RandomStringUtils.randomAlphanumeric(16);
        tokenVo.setTokenType("Bearer");
        tokenVo.setAccessToken(token);
        tokenVo.setRefreshToken(token);
        tokenVo.setExpiresIn(999999999);
        tokenVo.setScope("select");
        tokenVo.setTenantId("0");
        tokenVo.setLicense("made by martin");
        tokenVo.setDeptId(orgId);
        tokenVo.setDeptName(orgName);
        tokenVo.setUserId(userId);
        tokenVo.setUsername(userId);
        redisTemplateJackson.opsForValue().set(token, tokenVo);
        redisTemplateJackson.expire(token, 864000, TimeUnit.SECONDS);
        Cookie cookie = new Cookie(COOKIE_TOKEN, token);
        cookie.setMaxAge(864000);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //noinspection ConstantConditions
        requestAttributes.getResponse().addCookie(cookie);
        return tokenVo;
    }

    @Override
    public String logout() {
        String token = CookieUtils.getCookieValue(COOKIE_TOKEN);
        redisTemplateJackson.delete(token);
        return "登出成功";
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public String changePwd(String newPwd, TokenVo tokenVo) {
        UsersVo usersVo = (UsersVo) redisTemplateJackson.opsForHash().get(ERD_PREFIX + ":user", tokenVo.getUserId());
        //noinspection ConstantConditions
        usersVo.setPwd(DigestUtils.md5DigestAsHex(newPwd.getBytes(StandardCharsets.UTF_8)));
        usersVo.setUpdateTime(new Date());
        redisTemplateJackson.opsForHash().put(ERD_PREFIX + ":user", tokenVo.getUserId(), usersVo);
        return "修改成功";
    }
}
