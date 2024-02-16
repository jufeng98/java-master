package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.annos.ErdRolesAllowed
import org.javamaster.invocationlab.admin.enums.RoleEnum
import org.javamaster.invocationlab.admin.model.ResultVo
import org.javamaster.invocationlab.admin.model.erd.*
import org.javamaster.invocationlab.admin.service.ErdOnlineRoleService
import com.alibaba.fastjson.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

/**
 * @author yudong
 */
@RestController
@RequestMapping(value = ["/ncnb/project"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ErdOnlineRoleController {
    @Autowired
    private lateinit var erdOnlineRoleService: ErdOnlineRoleService

    @RequestMapping(value = ["/group/currentRolePermission"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getUserSubRoles(
        projectId: String,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<RolePermissionResVo> {
        return ResultVo.success(erdOnlineRoleService.getUserSubRoles(projectId, tokenVo))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_PERMISSION_GROUP)
    @RequestMapping(value = ["/group/roles"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getRoleGroups(projectId: String, @SessionAttribute("tokenVo") tokenVo: TokenVo): ResultVo<List<RoleResVo>> {
        return ResultVo.success(erdOnlineRoleService.getRoleGroups(projectId, tokenVo))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_ROLES_PAGE)
    @RequestMapping(value = ["/group/role/users"], method = [RequestMethod.GET])
    fun getRoleGroupUsers(
        projectId: String, roleId: String, current: Int, pageSize: Int,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<UsersResVo> {
        return ResultVo.success(
            erdOnlineRoleService.getRoleGroupUsers(
                projectId,
                roleId,
                current,
                pageSize,
                tokenVo
            )
        )
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_ROLES_SEARCH)
    @RequestMapping(value = ["/group/users"], method = [RequestMethod.GET])
    fun searchUsers(
        projectId: String, username: String,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<UsersResVo> {
        return ResultVo.success(erdOnlineRoleService.searchUsers(projectId, username, tokenVo))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_USERS_ADD)
    @RequestMapping(value = ["/group/role/users"], method = [RequestMethod.POST])
    fun addUsersToRoleGroup(
        @RequestBody jsonObjectReq: JSONObject,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<UsersResVo> {
        return ResultVo.success(erdOnlineRoleService.addUsersToRoleGroup(jsonObjectReq, tokenVo))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_ROLE_USERS_DEL)
    @RequestMapping(value = ["/group/role/users"], method = [RequestMethod.DELETE])
    fun delUsersFromRoleGroup(
        @RequestBody jsonObjectReq: JSONObject,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<UsersResVo> {
        return ResultVo.success(erdOnlineRoleService.delUsersFromRoleGroup(jsonObjectReq, tokenVo))
    }


    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_ROLE_PERMISSION)
    @RequestMapping(value = ["/group/role/permission"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getRoleGroupSubRoles(
        projectId: String, roleId: String,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<PermissionResVo> {
        return ResultVo.success(erdOnlineRoleService.getRoleGroupSubRoles(projectId, roleId, tokenVo))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_ROLE_PERMISSION_EDIT)
    @RequestMapping(value = ["/group/saveCheckedOperations"], method = [RequestMethod.GET, RequestMethod.POST])
    fun saveCheckedOperations(
        @RequestBody reqVo: SaveCheckedOperationsReqVo,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<String> {
        return ResultVo.success(erdOnlineRoleService.saveCheckedOperations(reqVo, tokenVo))
    }
}
