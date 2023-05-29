package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.annos.ErdRolesAllowed;
import org.javamaster.invocationlab.admin.enums.RoleEnum;
import org.javamaster.invocationlab.admin.model.ResultVo;
import org.javamaster.invocationlab.admin.model.erd.PermissionResVo;
import org.javamaster.invocationlab.admin.model.erd.RolePermissionResVo;
import org.javamaster.invocationlab.admin.model.erd.RoleResVo;
import org.javamaster.invocationlab.admin.model.erd.SaveCheckedOperationsReqVo;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.UsersResVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineRoleService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

/**
 * @author yudong
 */
@RestController
@RequestMapping(value = "/ncnb/project", produces = MediaType.APPLICATION_JSON_VALUE)
public class ErdOnlineRoleController {
    @Autowired
    private ErdOnlineRoleService erdOnlineRoleService;

    @RequestMapping(value = "/group/currentRolePermission", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<RolePermissionResVo> getUserSubRoles(String projectId,
                                                         @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineRoleService.getUserSubRoles(projectId, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_PERMISSION_GROUP)
    @RequestMapping(value = "/group/roles", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<List<RoleResVo>> getRoleGroups(String projectId, @SessionAttribute("tokenVo") TokenVo tokenVo) {
        return ResultVo.success(erdOnlineRoleService.getRoleGroups(projectId, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_ROLES_PAGE)
    @RequestMapping(value = "/group/role/users", method = {RequestMethod.GET})
    public ResultVo<UsersResVo> getRoleGroupUsers(String projectId, String roleId, Integer current, Integer pageSize,
                                                  @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineRoleService.getRoleGroupUsers(projectId, roleId, current, pageSize, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_ROLES_SEARCH)
    @RequestMapping(value = "/group/users", method = {RequestMethod.GET})
    public ResultVo<UsersResVo> searchUsers(String projectId, String username,
                                            @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineRoleService.searchUsers(projectId, username, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_USERS_ADD)
    @RequestMapping(value = "/group/role/users", method = {RequestMethod.POST})
    public ResultVo<UsersResVo> addUsersToRoleGroup(@RequestBody JSONObject jsonObjectReq,
                                                    @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineRoleService.addUsersToRoleGroup(jsonObjectReq, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_ROLE_USERS_DEL)
    @RequestMapping(value = "/group/role/users", method = {RequestMethod.DELETE})
    public ResultVo<UsersResVo> delUsersFromRoleGroup(@RequestBody JSONObject jsonObjectReq,
                                                      @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineRoleService.delUsersFromRoleGroup(jsonObjectReq, tokenVo));
    }


    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_ROLE_PERMISSION)
    @RequestMapping(value = "/group/role/permission", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<PermissionResVo> getRoleGroupSubRoles(String projectId, String roleId,
                                                          @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineRoleService.getRoleGroupSubRoles(projectId, roleId, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_ROLE_PERMISSION_EDIT)
    @RequestMapping(value = "/group/saveCheckedOperations", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<String> saveCheckedOperations(@RequestBody SaveCheckedOperationsReqVo reqVo,
                                                          @SessionAttribute("tokenVo") TokenVo tokenVo)  {
        return ResultVo.success(erdOnlineRoleService.saveCheckedOperations(reqVo, tokenVo));
    }

}
