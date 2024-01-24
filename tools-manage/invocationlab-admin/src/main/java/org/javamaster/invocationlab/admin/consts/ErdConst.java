package org.javamaster.invocationlab.admin.consts;

/**
 * @author yudong
 */
public class ErdConst {
    public static final String ADMIN = "admin";
    public static final String ADMIN_CODE = "100001";
    public static final String ERD_PREFIX = ADMIN + ":erd:";
    // cookie中不能包含:等特殊字符,否则报错
    public static final String COOKIE_TOKEN = ADMIN + "-erd-token";
    public static final String SUB_ROLE_SUFFIX = ":ROLE_IDS";

    public static final String PROJECT_DS = ERD_PREFIX + "project:ds";
    public static final String PROJECT_STATISTIC = ERD_PREFIX + "statistic";
    public static final String PROJECT_ROLE_GROUP_USERS = ERD_PREFIX + "project:roleGroup:user:";
    public static final String USER_PROJECT_ROLE_GROUP = ERD_PREFIX + "userRoleGroup:";
    public static final String PROJECT_ROLE_GROUP_SUB_ROLES = ERD_PREFIX + "roleGroup:subRole:";

    public static final String PROJECT_QUERY_TREE = ERD_PREFIX + "tree";
    public static final String QUERY_TREE_TREE_NODE = ERD_PREFIX + "treeNode";

    public static final String ANGEL_PRO_ALLOW = ERD_PREFIX + "angelProAllow";
    public static final String ANGEL_PRO_RPC_ALLOW = ERD_PREFIX + "angelProRpcAllow";

    public static final String QUERY_RES = ERD_PREFIX + "queryRes";
}
