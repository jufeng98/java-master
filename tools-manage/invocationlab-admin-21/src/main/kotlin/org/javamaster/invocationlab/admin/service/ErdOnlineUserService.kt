package org.javamaster.invocationlab.admin.service

import org.javamaster.invocationlab.admin.model.erd.TokenVo

/**
 * @author yudong
 */
interface ErdOnlineUserService {
    fun findUserName(code: String): String

    fun findUsers(name: String, code: String): List<Map<String, Any>>

    fun login(userId: String, password: String): TokenVo

    fun refreshToken(token: String)

    fun logout(): String

    fun changePwd(newPwd: String, tokenVo: TokenVo): String
}
