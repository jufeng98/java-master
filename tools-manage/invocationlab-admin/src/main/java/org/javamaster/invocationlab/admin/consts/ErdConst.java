package org.javamaster.invocationlab.admin.consts;

import java.util.concurrent.atomic.AtomicInteger;

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
    public static final String EXEC_COLLECTION_NAME = ERD_PREFIX + "queryTableName";
    public static final String MONGO_KEY_NAME = "_id";

    public static final String NULL_VALUE = "<null>";
    public static final String ROW_OPERATION_TYPE = "rowOperationType";
    public static final String KEY = "key";
    public static final String INDEX = "index";
    public static final AtomicInteger COUNTER = new AtomicInteger(0);
}
