package org.javamaster.invocationlab.admin.service.impl;

import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.enums.MenuEnum;
import org.javamaster.invocationlab.admin.enums.RoleEnum;
import org.javamaster.invocationlab.admin.enums.RoleGroupEnum;
import org.javamaster.invocationlab.admin.model.erd.CheckboxesVo;
import org.javamaster.invocationlab.admin.model.erd.OperationsVo;
import org.javamaster.invocationlab.admin.model.erd.PermissionResVo;
import org.javamaster.invocationlab.admin.model.erd.RolePermissionResVo;
import org.javamaster.invocationlab.admin.model.erd.RoleResVo;
import org.javamaster.invocationlab.admin.model.erd.SaveCheckedOperationsReqVo;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.UsersResVo;
import org.javamaster.invocationlab.admin.model.erd.UsersVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService;
import org.javamaster.invocationlab.admin.service.ErdOnlineRoleService;
import org.javamaster.invocationlab.admin.service.ErdOnlineUserService;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_ROLE_GROUP_SUB_ROLES;
import static org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_ROLE_GROUP_USERS;
import static org.javamaster.invocationlab.admin.consts.ErdConst.SUB_ROLE_SUFFIX;
import static org.javamaster.invocationlab.admin.consts.ErdConst.USER_PROJECT_ROLE_GROUP;

/**
 * @author yudong
 */
@SuppressWarnings("VulnerableCodeUsages")
@Service
public class ErdOnlineRoleServiceImpl implements ErdOnlineRoleService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplateJackson;
    @Autowired
    private ErdOnlineUserService erdOnlineUserService;
    @Autowired
    private ErdOnlineProjectService erdOnlineProjectService;

    @Override
    public RolePermissionResVo getUserSubRoles(String projectId, TokenVo tokenVo) throws Exception {
        Pair<Set<Long>, RoleGroupEnum> pair = getUserRoleIds(tokenVo.getUserId(), projectId);
        Set<Long> userRoleIds = pair.getKey();
        RolePermissionResVo resVo = new RolePermissionResVo();
        int loginRole = Integer.parseInt(pair.getValue().roleCode.split("_")[1]);
        resVo.setLoginRole(loginRole);
        List<String> list = Arrays.stream(RoleEnum.values())
                .filter(roleEnum -> userRoleIds.contains(roleEnum.id))
                .map(roleEnum -> roleEnum.roleCode).collect(Collectors.toList());
        resVo.setPermission(list);
        return resVo;
    }

    @Override
    public List<RoleResVo> getRoleGroups(String projectId, TokenVo tokenVo) {
        return Lists.newArrayList(
                initRoleResVo(projectId, RoleGroupEnum.OWNER),
                initRoleResVo(projectId, RoleGroupEnum.ADMIN),
                initRoleResVo(projectId, RoleGroupEnum.ORDINARY)
        );
    }

    private RoleResVo initRoleResVo(String projectId, RoleGroupEnum roleGroupEnum) {
        RoleResVo resVo = new RoleResVo();
        resVo.setId(roleGroupEnum.id);
        resVo.setProjectId(projectId);
        resVo.setRoleId(roleGroupEnum.roleId);
        resVo.setRoleCode(roleGroupEnum.roleCode);
        resVo.setRoleName(roleGroupEnum.roleName);
        return resVo;
    }

    @SuppressWarnings("unchecked")
    @Override
    public UsersResVo getRoleGroupUsers(String projectId, String roleId, Integer current, Integer pageSize, TokenVo tokenVo) {
        List<UsersVo> usersVos = (List<UsersVo>) redisTemplateJackson.opsForHash().get(PROJECT_ROLE_GROUP_USERS + projectId,
                RoleGroupEnum.getRoleGroupEnum(roleId).name());
        if (usersVos == null) {
            usersVos = Lists.newArrayList();
        }
        UsersResVo resVo = new UsersResVo();
        resVo.setTotal(usersVos.size());
        resVo.setSize(200);
        resVo.setCurrent(1);
        resVo.setSearchCount(true);
        resVo.setPages(1);
        resVo.setRecords(usersVos);
        return resVo;
    }

    @SuppressWarnings("unchecked")
    @Override
    public UsersResVo addUsersToRoleGroup(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        String roleId = jsonObjectReq.getString("roleId");
        JSONArray userIds = jsonObjectReq.getJSONArray("userIds");
        List<UsersVo> reqUsersVos = convertUserIds(userIds);

        reqUsersVos.forEach(it -> {
            RoleGroupEnum roleGroupEnum = getUserRoleGroup(it.getId(), projectId);
            if (roleGroupEnum != null) {
                throw new ErdException("添加失败,用户" + it.getId() + "已经拥有" + roleGroupEnum.roleName + "角色!");
            }
        });

        RoleGroupEnum roleGroupEnum = RoleGroupEnum.getRoleGroupEnum(roleId);
        List<UsersVo> usersVos = (List<UsersVo>) redisTemplateJackson.opsForHash().get(PROJECT_ROLE_GROUP_USERS + projectId,
                roleGroupEnum.name());
        if (usersVos == null) {
            usersVos = Lists.newArrayList();
        }
        usersVos.addAll(reqUsersVos);
        redisTemplateJackson.opsForHash().put(PROJECT_ROLE_GROUP_USERS + projectId, roleGroupEnum.name(), usersVos);
        erdOnlineProjectService.saveRecordsVoToGroupUsers(projectId, reqUsersVos, roleGroupEnum, tokenVo);
        return new UsersResVo();
    }

    private List<UsersVo> convertUserIds(JSONArray userIds) {
        return userIds.stream().map(obj -> {
            String userId = obj.toString();
            UsersVo usersVo = new UsersVo();
            usersVo.setId(userId);
            try {
                String userName = erdOnlineUserService.findUserName(userId);
                usersVo.setUsername(userName);
            } catch (ErdException e) {
                usersVo.setUsername("");
            }
            return usersVo;
        }).collect(Collectors.toList());
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    @Override
    public UsersResVo delUsersFromRoleGroup(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        String roleId = jsonObjectReq.getString("roleId");
        JSONArray userIds = jsonObjectReq.getJSONArray("userIds");
        List<UsersVo> reqUsersVos = convertUserIds(userIds);
        RoleGroupEnum roleGroupEnum = RoleGroupEnum.getRoleGroupEnum(roleId);
        List<UsersVo> usersVos = (List<UsersVo>) redisTemplateJackson.opsForHash().get(PROJECT_ROLE_GROUP_USERS + projectId,
                roleGroupEnum.name());
        //noinspection ConstantConditions
        usersVos.removeIf(usersVo -> reqUsersVos.stream().anyMatch(reqUsersVo -> reqUsersVo.getId().equals(usersVo.getId())));
        redisTemplateJackson.opsForHash().put(PROJECT_ROLE_GROUP_USERS + projectId, roleGroupEnum.name(), usersVos);
        erdOnlineProjectService.delRecordsVoFromGroupUsers(projectId, reqUsersVos, tokenVo);
        return new UsersResVo();
    }

    @Override
    public UsersResVo searchUsers(String projectId, String username, TokenVo tokenVo) {
        UsersResVo resVo = new UsersResVo();
        resVo.setSize(200);
        resVo.setCurrent(1);
        resVo.setSearchCount(true);
        resVo.setPages(1);
        List<Map<String, Object>> users = Lists.newArrayList();
        if (StringUtils.isNotBlank(username)) {
            users = erdOnlineUserService.findUsers(username, "");
        }
        List<UsersVo> usersVos = users.stream().map(jsonObject -> {
            UsersVo usersVo = new UsersVo();
            usersVo.setId(String.valueOf(jsonObject.get("empCode")));
            usersVo.setUsername(String.valueOf(jsonObject.get("empName")));
            usersVo.setEmail(String.valueOf(jsonObject.get("email")));
            return usersVo;
        }).collect(Collectors.toList());
        resVo.setRecords(usersVos);
        resVo.setTotal(usersVos.size());
        return resVo;
    }

    @Override
    public PermissionResVo getRoleGroupSubRoles(String projectId, String roleId, TokenVo tokenVo) throws IOException {
        RoleGroupEnum roleGroupEnum = RoleGroupEnum.getRoleGroupEnum(roleId);

        String userRoleIdsStr = (String) redisTemplateJackson.opsForHash().get(PROJECT_ROLE_GROUP_SUB_ROLES + projectId,
                roleGroupEnum.name() + SUB_ROLE_SUFFIX);
        Set<Long> subRoleIds = JsonUtils.mapper.readValue(userRoleIdsStr, new TypeReference<HashSet<Long>>() {
        });

        PermissionResVo permissionResVo = new PermissionResVo();
        permissionResVo.setLoginRole(0);
        Map<MenuEnum, List<RoleEnum>> menuRoleEnumsMap = Arrays.stream(RoleEnum.values())
                .collect(Collectors.groupingBy(roleEnum -> roleEnum.menuEnum));
        List<CheckboxesVo> checkboxesVos = menuRoleEnumsMap.entrySet().stream().map(entry -> {
            MenuEnum menuEnum = entry.getKey();
            List<RoleEnum> roleEnums = entry.getValue();
            CheckboxesVo checkboxesVo = new CheckboxesVo();
            checkboxesVo.setMenuId(menuEnum.name());
            checkboxesVo.setMenuName(menuEnum.menuName);
            List<Long> defaultValue = Lists.newArrayList();
            List<OperationsVo> operationsVos = roleEnums.stream().map(roleEnum -> {
                OperationsVo operationsVo = new OperationsVo();
                operationsVo.setName(roleEnum.roleName);
                operationsVo.setValue(roleEnum.id);
                if (subRoleIds.contains(roleEnum.id)) {
                    defaultValue.add(roleEnum.id);
                }
                return operationsVo;
            }).collect(Collectors.toList());
            checkboxesVo.setOperations(operationsVos);
            checkboxesVo.setDefaultValue(defaultValue);
            return checkboxesVo;
        }).collect(Collectors.toList());
        permissionResVo.setCheckboxes(checkboxesVos);
        return permissionResVo;
    }

    @Override
    public String saveCheckedOperations(SaveCheckedOperationsReqVo reqVo, TokenVo tokenVo) {
        RoleGroupEnum roleGroupEnum = RoleGroupEnum.getRoleGroupEnum(reqVo.getRoleId());
        saveRoleGroupSubRolesToRedis(reqVo.getProjectId(), reqVo.getCheckedKeys(), roleGroupEnum);
        return "保存成功";
    }

    @Override
    public void saveRoleGroupSubRolesToRedis(String projectId, Set<Long> roleIds, RoleGroupEnum roleGroupEnum) {
        redisTemplateJackson.opsForHash().put(PROJECT_ROLE_GROUP_SUB_ROLES + projectId, roleGroupEnum.name() + SUB_ROLE_SUFFIX,
                JsonUtils.objectToString(roleIds));
    }

    @Override
    public Pair<Set<Long>, RoleGroupEnum> getUserRoleIds(String userId, String projectId) throws Exception {
        RoleGroupEnum userRoleGroupEnum = getUserRoleGroup(userId, projectId);
        String userRoleIdsStr = (String) redisTemplateJackson.opsForHash().get(PROJECT_ROLE_GROUP_SUB_ROLES + projectId,
                userRoleGroupEnum.name() + SUB_ROLE_SUFFIX);
        Set<Long> ids = JsonUtils.mapper.readValue(userRoleIdsStr, new TypeReference<HashSet<Long>>() {
        });
        return Pair.of(ids, userRoleGroupEnum);
    }

    @Override
    public RoleGroupEnum getUserRoleGroup(String userId, String projectId) {
        String userRoleGroupName = (String) redisTemplateJackson.opsForHash().get(USER_PROJECT_ROLE_GROUP + userId, projectId);
        if (StringUtils.isBlank(userRoleGroupName)) {
            return null;
        }
        return RoleGroupEnum.valueOf(userRoleGroupName);
    }

    @Override
    public void saveRolesWhenCreateProject(String projectId, TokenVo tokenVo) {
        redisTemplateJackson.opsForHash().put(USER_PROJECT_ROLE_GROUP + tokenVo.getUserId(), projectId,
                RoleGroupEnum.OWNER.name());

        saveSubRolesToRedis(projectId, RoleGroupEnum.OWNER);
        saveSubRolesToRedis(projectId, RoleGroupEnum.ADMIN);
        saveSubRolesToRedis(projectId, RoleGroupEnum.ORDINARY);

        UsersVo usersVo = new UsersVo();
        usersVo.setId(tokenVo.getUserId());
        usersVo.setUsername(tokenVo.getUsername());
        usersVo.setDeptId(tokenVo.getDeptId());
        redisTemplateJackson.opsForHash().put(PROJECT_ROLE_GROUP_USERS + projectId, RoleGroupEnum.OWNER.name(),
                Lists.newArrayList(usersVo));
    }

    private void saveSubRolesToRedis(String projectId, RoleGroupEnum roleGroupEnum) {
        Set<Long> roleIds = RoleGroupEnum.getDefaultRoleEnums(roleGroupEnum);
        saveRoleGroupSubRolesToRedis(projectId, roleIds, roleGroupEnum);
    }

    @Override
    public void delRolesWhenDelProject(String projectId, TokenVo tokenVo) {
        redisTemplateJackson.opsForHash().delete(USER_PROJECT_ROLE_GROUP + tokenVo.getUserId(), projectId);
        redisTemplateJackson.delete(PROJECT_ROLE_GROUP_SUB_ROLES + projectId);
        redisTemplateJackson.delete(PROJECT_ROLE_GROUP_USERS + projectId);
    }

}
