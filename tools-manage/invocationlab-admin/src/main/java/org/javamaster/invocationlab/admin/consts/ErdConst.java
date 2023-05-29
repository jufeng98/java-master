package org.javamaster.invocationlab.admin.consts;

/**
 * @author yudong
 */
public class ErdConst {
    private static final String ADMIN = "admin";
    public static final String ERD_PREFIX = ADMIN + ":erd:";
    public static final String COOKIE_TOKEN = ADMIN + "-erd-token";
    public static final String SUB_ROLE_SUFFIX = ":ROLE_IDS";

    public static final String PROJECT_DS = ERD_PREFIX + "project:ds";
    public static final String PROJECT_ROLE_GROUP_USERS = ERD_PREFIX + "project:roleGroup:user:";
    public static final String USER_PROJECT_ROLE_GROUP = ERD_PREFIX + "userRoleGroup:";
    public static final String PROJECT_ROLE_GROUP_SUB_ROLES = ERD_PREFIX + "roleGroup:subRole:";

    public static final String PROJECT_QUERY_TREE = ERD_PREFIX + "tree";
    public static final String QUERY_TREE_TREE_NODE = ERD_PREFIX + "treeNode";
}
