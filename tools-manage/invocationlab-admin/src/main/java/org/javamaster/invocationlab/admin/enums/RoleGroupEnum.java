package org.javamaster.invocationlab.admin.enums;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yudong
 */
public enum RoleGroupEnum {
    OWNER(1625737465778208769L, "d2f5628dc54dd72b1dfaa6d15368180b", "590d4773_0", "团队所有者"),
    ADMIN(1625737465786597377L, "8310eb4e2dae9a72ff4d82f8c561841b", "590d4773_1", "团队管理员"),
    ORDINARY(1625737465790791682L, "ab2809bc8d67fd3e78157300b907ef7f", "590d4773_2", "团队普通成员"),
    ;

    public final long id;
    public final String roleId;
    public final String roleCode;
    public final String roleName;

    RoleGroupEnum(long id, String roleId, String roleCode, String roleName) {
        this.id = id;
        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleName = roleName;
    }

    public static RoleGroupEnum getRoleGroupEnum(String roleId) {
        for (RoleGroupEnum value : RoleGroupEnum.values()) {
            if (value.roleId.equals(roleId)) {
                return value;
            }
        }
        throw new RuntimeException("wrong:" + roleId);
    }

    public static Set<Long> getDefaultRoleEnums(RoleGroupEnum roleGroupEnum) {
        if (roleGroupEnum == OWNER) {
            return Arrays.stream(RoleEnum.values())
                    .map(roleEnum -> roleEnum.id)
                    .collect(Collectors.toSet());
        } else if (roleGroupEnum == ADMIN) {
            Set<Long> ids = Arrays.stream(RoleEnum.values())
                    .map(roleEnum -> roleEnum.id)
                    .collect(Collectors.toSet());
            ids.remove(RoleEnum.ERD_PROJECT_GROUP_EDIT.id);
            ids.remove(RoleEnum.ERD_PROJECT_GROUP_DEL.id);
            return ids;
        } else if (roleGroupEnum == ORDINARY) {
            return Sets.newHashSet(RoleEnum.ERD_PROJECT_VIEW.id, RoleEnum.ERD_TABLE_EXPORT.id,
                    RoleEnum.ERD_TABLE_EXPORT_COMMON.id, RoleEnum.ERD_TABLE_EXPORT_MORE.id);
        } else {
            throw new RuntimeException("wrong:" + roleGroupEnum);
        }
    }
}
