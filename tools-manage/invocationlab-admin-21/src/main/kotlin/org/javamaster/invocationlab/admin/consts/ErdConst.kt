package org.javamaster.invocationlab.admin.consts

/**
 * @author yudong
 */
object ErdConst {
    private const val MH_ERD: String = "mh-erd"
    const val ADMIN_CODE: String = "1000001"
    const val ERD_PREFIX: String = "$MH_ERD:erd:"

    // cookie中不能包含:等特殊字符,否则报错
    const val COOKIE_TOKEN: String = "$MH_ERD-erd-token"
    const val SUB_ROLE_SUFFIX: String = ":ROLE_IDS"

    const val PROJECT_DS: String = ERD_PREFIX + "project:ds"
    const val PROJECT_STATISTIC: String = ERD_PREFIX + "statistic"
    const val PROJECT_ROLE_GROUP_USERS: String = ERD_PREFIX + "project:roleGroup:user:"
    const val USER_PROJECT_ROLE_GROUP: String = ERD_PREFIX + "userRoleGroup:"
    const val PROJECT_ROLE_GROUP_SUB_ROLES: String = ERD_PREFIX + "roleGroup:subRole:"

    const val PROJECT_QUERY_TREE: String = ERD_PREFIX + "tree"
    const val QUERY_TREE_TREE_NODE: String = ERD_PREFIX + "treeNode"

    const val ANGEL_PRO_ALLOW: String = ERD_PREFIX + "angelProAllow"
    const val ANGEL_PRO_RPC_ALLOW: String = ERD_PREFIX + "angelProRpcAllow"

    const val QUERY_RES: String = ERD_PREFIX + "queryRes"
}
