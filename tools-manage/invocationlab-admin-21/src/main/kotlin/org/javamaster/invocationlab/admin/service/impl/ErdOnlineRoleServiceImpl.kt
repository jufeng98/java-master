package org.javamaster.invocationlab.admin.service.impl

import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_ROLE_GROUP_SUB_ROLES
import org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_ROLE_GROUP_USERS
import org.javamaster.invocationlab.admin.consts.ErdConst.SUB_ROLE_SUFFIX
import org.javamaster.invocationlab.admin.consts.ErdConst.USER_PROJECT_ROLE_GROUP
import org.javamaster.invocationlab.admin.enums.MenuEnum
import org.javamaster.invocationlab.admin.enums.RoleEnum
import org.javamaster.invocationlab.admin.enums.RoleGroupEnum
import org.javamaster.invocationlab.admin.enums.RoleGroupEnum.Companion.getDefaultRoleEnums
import org.javamaster.invocationlab.admin.enums.RoleGroupEnum.Companion.getRoleGroupEnum
import org.javamaster.invocationlab.admin.model.erd.*
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService
import org.javamaster.invocationlab.admin.service.ErdOnlineRoleService
import org.javamaster.invocationlab.admin.service.ErdOnlineUserService
import org.javamaster.invocationlab.admin.util.JsonUtils
import org.javamaster.invocationlab.admin.util.JsonUtils.objectToString
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.collect.Lists
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Pair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * @author yudong
 */
@Service
class ErdOnlineRoleServiceImpl : ErdOnlineRoleService {
    @Autowired
    private lateinit var redisTemplateJackson: RedisTemplate<String, Any>

    @Autowired
    private lateinit var erdOnlineUserService: ErdOnlineUserService

    @Autowired
    private lateinit var erdOnlineProjectService: ErdOnlineProjectService


    override fun getUserSubRoles(projectId: String, tokenVo: TokenVo): RolePermissionResVo {
        val pair = getUserRoleIds(
            tokenVo.userId!!, projectId
        )
        val userRoleIds = pair.key
        val resVo = RolePermissionResVo()
        val loginRole =
            pair.value.roleCode.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toInt()
        resVo.loginRole = loginRole
        val list = Arrays.stream(RoleEnum.entries.toTypedArray())
            .filter { roleEnum: RoleEnum -> userRoleIds.contains(roleEnum.id) }
            .map { roleEnum: RoleEnum -> roleEnum.roleCode }.collect(Collectors.toList())
        resVo.permission = list
        return resVo
    }

    override fun getRoleGroups(projectId: String, tokenVo: TokenVo): List<RoleResVo> {
        return Lists.newArrayList(
            initRoleResVo(projectId, RoleGroupEnum.OWNER),
            initRoleResVo(projectId, RoleGroupEnum.ADMIN),
            initRoleResVo(projectId, RoleGroupEnum.ORDINARY)
        )
    }

    private fun initRoleResVo(projectId: String, roleGroupEnum: RoleGroupEnum): RoleResVo {
        val resVo = RoleResVo()
        resVo.id = roleGroupEnum.id
        resVo.projectId = projectId
        resVo.roleId = roleGroupEnum.roleId
        resVo.roleCode = roleGroupEnum.roleCode
        resVo.roleName = roleGroupEnum.roleName
        return resVo
    }

    override fun getRoleGroupUsers(
        projectId: String,
        roleId: String,
        current: Int,
        pageSize: Int,
        tokenVo: TokenVo
    ): UsersResVo {
        @Suppress("UNCHECKED_CAST")
        var usersVos =
            redisTemplateJackson.opsForHash<Any, Any>()[PROJECT_ROLE_GROUP_USERS + projectId, getRoleGroupEnum(roleId).name] as List<UsersVo>?
        if (usersVos.isNullOrEmpty()) {
            usersVos = mutableListOf()
        }
        val resVo = UsersResVo()
        resVo.total = usersVos.size
        resVo.size = 200
        resVo.current = 1
        resVo.searchCount = true
        resVo.pages = 1
        resVo.records = usersVos
        return resVo
    }


    override fun addUsersToRoleGroup(jsonObjectReq: JSONObject, tokenVo: TokenVo): UsersResVo {
        val projectId = jsonObjectReq.getString("projectId")
        val roleId = jsonObjectReq.getString("roleId")
        val userIds = jsonObjectReq.getJSONArray("userIds")
        val reqUsersVos = convertUserIds(userIds)

        reqUsersVos.forEach(Consumer { it: UsersVo ->
            val roleGroupEnum = getUserRoleGroup(it.id!!, projectId)
            if (roleGroupEnum != null) {
                throw ErdException("添加失败,用户" + it.id + "已经拥有" + roleGroupEnum.roleName + "角色!")
            }
        })

        val roleGroupEnum = getRoleGroupEnum(roleId)

        @Suppress("UNCHECKED_CAST")
        var usersVos =
            redisTemplateJackson.opsForHash<Any, Any>()[PROJECT_ROLE_GROUP_USERS + projectId, roleGroupEnum.name] as MutableList<UsersVo>?
        if (usersVos == null) {
            usersVos = mutableListOf()
        }
        usersVos.addAll(reqUsersVos)
        redisTemplateJackson.opsForHash<Any, Any?>()
            .put(PROJECT_ROLE_GROUP_USERS + projectId, roleGroupEnum.name, usersVos)
        erdOnlineProjectService.saveRecordsVoToGroupUsers(projectId, reqUsersVos, roleGroupEnum, tokenVo)
        return UsersResVo()
    }

    private fun convertUserIds(userIds: JSONArray): List<UsersVo> {
        return userIds.stream().map { obj: Any ->
            val userId = obj.toString()
            val usersVo = UsersVo()
            usersVo.id = userId
            try {
                val userName = erdOnlineUserService.findUserName(userId)
                usersVo.username = userName
            } catch (e: ErdException) {
                usersVo.username = ""
            }
            usersVo
        }.collect(Collectors.toList())
    }


    override fun delUsersFromRoleGroup(jsonObjectReq: JSONObject, tokenVo: TokenVo): UsersResVo {
        val projectId = jsonObjectReq.getString("projectId")
        val roleId = jsonObjectReq.getString("roleId")
        val userIds = jsonObjectReq.getJSONArray("userIds")
        val reqUsersVos = convertUserIds(userIds)
        val roleGroupEnum = getRoleGroupEnum(roleId)

        @Suppress("UNCHECKED_CAST")
        val usersVos =
            redisTemplateJackson.opsForHash<Any, Any>()[PROJECT_ROLE_GROUP_USERS + projectId, roleGroupEnum.name] as MutableList<UsersVo>
        usersVos.removeIf { usersVo: UsersVo ->
            reqUsersVos.stream().anyMatch { reqUsersVo: UsersVo -> reqUsersVo.id == usersVo.id }
        }
        redisTemplateJackson.opsForHash<Any, Any?>()
            .put(PROJECT_ROLE_GROUP_USERS + projectId, roleGroupEnum.name, usersVos)
        erdOnlineProjectService.delRecordsVoFromGroupUsers(projectId, reqUsersVos, tokenVo)
        return UsersResVo()
    }

    override fun searchUsers(projectId: String, username: String, tokenVo: TokenVo): UsersResVo {
        val resVo = UsersResVo()
        resVo.size = 200
        resVo.current = 1
        resVo.searchCount = true
        resVo.pages = 1
        var users: List<Map<String, Any>> = Lists.newArrayList()
        if (StringUtils.isNotBlank(username)) {
            users = erdOnlineUserService.findUsers(username, "")
        }
        val usersVos = users.stream().map { jsonObject: Map<String, Any> ->
            val usersVo = UsersVo()
            usersVo.id = jsonObject["empCode"].toString()
            usersVo.username = jsonObject["empName"].toString()
            usersVo.email = jsonObject["email"].toString()
            usersVo
        }.collect(Collectors.toList())
        resVo.records = usersVos
        resVo.total = usersVos.size
        return resVo
    }

    @Throws(IOException::class)
    override fun getRoleGroupSubRoles(projectId: String, roleId: String, tokenVo: TokenVo): PermissionResVo {
        val roleGroupEnum = getRoleGroupEnum(roleId)

        val userRoleIdsStr =
            redisTemplateJackson.opsForHash<Any, Any>()[PROJECT_ROLE_GROUP_SUB_ROLES + projectId, roleGroupEnum.name + SUB_ROLE_SUFFIX] as String?
        val subRoleIds: Set<Long> =
            JsonUtils.mapper.readValue(userRoleIdsStr, object : TypeReference<HashSet<Long>>() {
            })

        val permissionResVo = PermissionResVo()
        permissionResVo.loginRole = 0
        val menuRoleEnumsMap = Arrays.stream(RoleEnum.entries.toTypedArray())
            .collect(
                Collectors.groupingBy { roleEnum: RoleEnum -> roleEnum.menuEnum }
            )
        val checkboxesVos =
            menuRoleEnumsMap.entries.stream().map { entry: Map.Entry<MenuEnum, List<RoleEnum>> ->
                val menuEnum = entry.key
                val roleEnums = entry.value
                val checkboxesVo = CheckboxesVo()
                checkboxesVo.menuId = menuEnum.name
                checkboxesVo.menuName = menuEnum.menuName
                val defaultValue: MutableList<Long> = Lists.newArrayList()
                val operationsVos = roleEnums.stream().map { roleEnum: RoleEnum ->
                    val operationsVo = OperationsVo()
                    operationsVo.name = roleEnum.roleName
                    operationsVo.value = roleEnum.id
                    if (subRoleIds.contains(roleEnum.id)) {
                        defaultValue.add(roleEnum.id)
                    }
                    operationsVo
                }.collect(Collectors.toList())
                checkboxesVo.operations = operationsVos
                checkboxesVo.defaultValue = defaultValue
                checkboxesVo
            }.collect(Collectors.toList())
        permissionResVo.checkboxes = checkboxesVos
        return permissionResVo
    }

    override fun saveCheckedOperations(reqVo: SaveCheckedOperationsReqVo, tokenVo: TokenVo): String {
        val roleGroupEnum = getRoleGroupEnum(reqVo.roleId!!)
        saveRoleGroupSubRolesToRedis(reqVo.projectId!!, reqVo.checkedKeys!!, roleGroupEnum)
        return "保存成功"
    }

    override fun saveRoleGroupSubRolesToRedis(projectId: String, roleIds: Set<Long>, roleGroupEnum: RoleGroupEnum) {
        redisTemplateJackson.opsForHash<Any, Any>().put(
            PROJECT_ROLE_GROUP_SUB_ROLES + projectId, roleGroupEnum.name + SUB_ROLE_SUFFIX,
            objectToString(roleIds)
        )
    }


    override fun getUserRoleIds(userId: String, projectId: String): Pair<Set<Long>, RoleGroupEnum> {
        val userRoleGroupEnum = getUserRoleGroup(userId, projectId)
        val userRoleIdsStr =
            redisTemplateJackson.opsForHash<Any, Any>()[PROJECT_ROLE_GROUP_SUB_ROLES + projectId, userRoleGroupEnum!!.name + SUB_ROLE_SUFFIX] as String
        val ids: Set<Long> =
            JsonUtils.mapper.readValue(userRoleIdsStr, object : TypeReference<HashSet<Long>>() {
            })
        return Pair.of(ids, userRoleGroupEnum)
    }

    override fun getUserRoleGroup(userId: String, projectId: String): RoleGroupEnum? {
        val userRoleGroupName =
            redisTemplateJackson.opsForHash<Any, Any>()[USER_PROJECT_ROLE_GROUP + userId, projectId] as String?
        if (StringUtils.isBlank(userRoleGroupName)) {
            return null
        }
        return RoleGroupEnum.valueOf(userRoleGroupName!!)
    }

    override fun saveRolesWhenCreateProject(projectId: String, tokenVo: TokenVo) {
        redisTemplateJackson.opsForHash<Any, Any>().put(
            USER_PROJECT_ROLE_GROUP + tokenVo.userId, projectId,
            RoleGroupEnum.OWNER.name
        )

        saveSubRolesToRedis(projectId, RoleGroupEnum.OWNER)
        saveSubRolesToRedis(projectId, RoleGroupEnum.ADMIN)
        saveSubRolesToRedis(projectId, RoleGroupEnum.ORDINARY)

        val usersVo = UsersVo()
        usersVo.id = tokenVo.userId
        usersVo.username = tokenVo.username
        usersVo.deptId = tokenVo.deptId
        redisTemplateJackson.opsForHash<Any, Any>().put(
            PROJECT_ROLE_GROUP_USERS + projectId, RoleGroupEnum.OWNER.name,
            Lists.newArrayList(usersVo)
        )
    }

    private fun saveSubRolesToRedis(projectId: String, roleGroupEnum: RoleGroupEnum) {
        val roleIds = getDefaultRoleEnums(roleGroupEnum)
        saveRoleGroupSubRolesToRedis(projectId, roleIds, roleGroupEnum)
    }

    override fun delRolesWhenDelProject(projectId: String, tokenVo: TokenVo) {
        redisTemplateJackson.opsForHash<Any, Any>().delete(USER_PROJECT_ROLE_GROUP + tokenVo.userId, projectId)
        redisTemplateJackson.delete(PROJECT_ROLE_GROUP_SUB_ROLES + projectId)
        redisTemplateJackson.delete(PROJECT_ROLE_GROUP_USERS + projectId)
    }
}
