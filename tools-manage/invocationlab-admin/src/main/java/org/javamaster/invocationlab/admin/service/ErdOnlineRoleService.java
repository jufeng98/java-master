package org.javamaster.invocationlab.admin.service;

import org.javamaster.invocationlab.admin.enums.RoleGroupEnum;
import org.javamaster.invocationlab.admin.model.erd.PermissionResVo;
import org.javamaster.invocationlab.admin.model.erd.RolePermissionResVo;
import org.javamaster.invocationlab.admin.model.erd.RoleResVo;
import org.javamaster.invocationlab.admin.model.erd.SaveCheckedOperationsReqVo;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.UsersResVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

/**
 * @author yudong
 */
public interface ErdOnlineRoleService {
    RolePermissionResVo getUserSubRoles(String projectId, TokenVo tokenVo) throws Exception;

    List<RoleResVo> getRoleGroups(String projectId, TokenVo tokenVo);

    UsersResVo getRoleGroupUsers(String projectId, String roleId, Integer current, Integer pageSize, TokenVo tokenVo) throws Exception;

    UsersResVo searchUsers(String projectId, String username, TokenVo tokenVo) throws Exception;

    UsersResVo addUsersToRoleGroup(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

    UsersResVo delUsersFromRoleGroup(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

    PermissionResVo getRoleGroupSubRoles(String projectId, String roleId, TokenVo tokenVo) throws Exception;

    String saveCheckedOperations(SaveCheckedOperationsReqVo reqVo, TokenVo tokenVo);

    void saveRoleGroupSubRolesToRedis(String projectId, Set<Long> roleIds, RoleGroupEnum roleGroupEnum);

    Pair<Set<Long>, RoleGroupEnum> getUserRoleIds(String userId, String projectId) throws Exception;

    RoleGroupEnum getUserRoleGroup(String userId, String projectId);

    void saveRolesWhenCreateProject(String projectId, TokenVo tokenVo);

    void delRolesWhenDelProject(String projectId, TokenVo tokenVo);
}
