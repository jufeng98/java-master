package org.javamaster.invocationlab.admin.service

import org.javamaster.invocationlab.admin.enums.RoleGroupEnum
import org.javamaster.invocationlab.admin.model.erd.*
import com.alibaba.fastjson.JSONObject
import org.apache.commons.lang3.tuple.Pair

/**
 * @author yudong
 */
interface ErdOnlineRoleService {
    fun getUserSubRoles(projectId: String, tokenVo: TokenVo): RolePermissionResVo

    fun getRoleGroups(projectId: String, tokenVo: TokenVo): List<RoleResVo>

    fun getRoleGroupUsers(
        projectId: String,
        roleId: String,
        current: Int,
        pageSize: Int,
        tokenVo: TokenVo
    ): UsersResVo

    fun searchUsers(projectId: String, username: String, tokenVo: TokenVo): UsersResVo

    fun addUsersToRoleGroup(jsonObjectReq: JSONObject, tokenVo: TokenVo): UsersResVo

    fun delUsersFromRoleGroup(jsonObjectReq: JSONObject, tokenVo: TokenVo): UsersResVo

    fun getRoleGroupSubRoles(projectId: String, roleId: String, tokenVo: TokenVo): PermissionResVo

    fun saveCheckedOperations(reqVo: SaveCheckedOperationsReqVo, tokenVo: TokenVo): String

    fun saveRoleGroupSubRolesToRedis(projectId: String, roleIds: Set<Long>, roleGroupEnum: RoleGroupEnum)

    fun getUserRoleIds(userId: String, projectId: String): Pair<Set<Long>, RoleGroupEnum>

    fun getUserRoleGroup(userId: String, projectId: String): RoleGroupEnum?

    fun saveRolesWhenCreateProject(projectId: String, tokenVo: TokenVo)

    fun delRolesWhenDelProject(projectId: String, tokenVo: TokenVo)
}
