package org.javamaster.invocationlab.admin.security.entity;

/**
 * @author yudong
 */
public enum RoleType {
    //各个角色定义
    DEV, QA, QAS, ADMIN;

    public static boolean compare(RoleType ori, RoleType tar) {
        if (ori == RoleType.DEV) {
            if (tar == RoleType.DEV || tar == RoleType.ADMIN) {
                return true;
            }
        }
        if (ori == RoleType.QA) {
            if (tar == RoleType.QA || tar == RoleType.ADMIN) {
                return true;
            }
        }
        if (ori == RoleType.QAS) {
            if (tar == RoleType.QAS || tar == RoleType.QA || tar == RoleType.ADMIN) {
                return true;
            }
        }
        if (ori == RoleType.ADMIN) {
            return tar == RoleType.ADMIN;
        }
        return false;
    }
}
