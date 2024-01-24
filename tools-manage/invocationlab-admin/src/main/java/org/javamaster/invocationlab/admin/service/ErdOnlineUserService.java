package org.javamaster.invocationlab.admin.service;

import org.javamaster.invocationlab.admin.model.erd.TokenVo;

import java.util.List;
import java.util.Map;

/**
 * @author yudong
 */
public interface ErdOnlineUserService {
    String findUserName(String code);

    List<Map<String, Object>> findUsers(String name, String code);

    TokenVo login(String userId, String password);

    void refreshToken(String token);

    String logout();

    String changePwd(String newPwd, TokenVo tokenVo);
}
