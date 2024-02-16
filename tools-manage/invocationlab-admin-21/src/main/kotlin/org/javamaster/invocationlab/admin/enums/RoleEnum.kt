package org.javamaster.invocationlab.admin.enums

/**
 * @author yudong
 */
enum class RoleEnum(
    @JvmField val menuEnum: MenuEnum,
    @JvmField val id: Long,
    @JvmField val roleCode: String,
    @JvmField val roleName: String
) {
    ERD_PROJECT_GROUP_EDIT(MenuEnum.TEAM_BASE_SETUP, 100030513200234531L, "erd_project_group_edit", "修改"),
    ERD_PROJECT_GROUP_DEL(MenuEnum.TEAM_BASE_SETUP, 100030513200234532L, "erd_project_group_del", "删除"),

    ERD_PROJECT_PERMISSION_GROUP(
        MenuEnum.TEAM_PERMISSION_GROUP,
        100030513200234673L,
        "erd_project_permission_group",
        "查看页面"
    ),
    ERD_PROJECT_ROLES_PAGE(MenuEnum.TEAM_PERMISSION_GROUP, 100030513200234533L, "erd_project_roles_page", "用户组成员"),
    ERD_PROJECT_ROLES_SEARCH(MenuEnum.TEAM_PERMISSION_GROUP, 100030513200234534L, "erd_project_roles_search", "搜索"),
    ERD_PROJECT_USERS_ADD(MenuEnum.TEAM_PERMISSION_GROUP, 100030513200234535L, "erd_project_users_add", "新增用户"),
    ERD_PROJECT_ROLE_USERS_DEL(
        MenuEnum.TEAM_PERMISSION_GROUP,
        100030513200234536L,
        "erd_project_role_users_del",
        "移除用户"
    ),
    ERD_PROJECT_ROLE_PERMISSION(
        MenuEnum.TEAM_PERMISSION_GROUP,
        100030513200234537L,
        "erd_project_role_permission",
        "权限配置"
    ),
    ERD_PROJECT_ROLE_PERMISSION_EDIT(
        MenuEnum.TEAM_PERMISSION_GROUP,
        100030513200234538L,
        "erd_project_role_permission_edit",
        "修改权限"
    ),

    ERD_PROJECT_VIEW(MenuEnum.MODEL_DESIGN, 100030513200234539L, "erd_project_view", "查看模型"),
    ERD_PROJECT_SAVE(
        MenuEnum.MODEL_DESIGN,
        100030513200234540L,
        "erd_project_save",
        "修改模型"
    ),  //    ERD_HISPROJECT_LOAD(MenuEnum.VERSION_MANAGE, 100030513200234541L, "erd_hisProject_load", "查看页面"),
    //    ERD_HISPROJECT_ALL(MenuEnum.VERSION_MANAGE, 100030513200234542L, "erd_hisProject_all", "全部版本"),
    //    ERD_CONNECTOR_DBVERSION(MenuEnum.VERSION_MANAGE, 100030513200234543L, "erd_connector_dbversion", "获取数据源版本"),
    //    ERD_HISPROJECT_ADD(MenuEnum.VERSION_MANAGE, 100030513200234544L, "erd_hisProject_add", "新增"),
    //    ERD_HISPROJECT_CONFIG(MenuEnum.VERSION_MANAGE, 100030513200234545L, "erd_hisProject_config", "同步配置"),
    //    ERD_HISPROJECT_INIT(MenuEnum.VERSION_MANAGE, 100030513200234546L, "erd_hisProject_init", "初始化基线"),
    //    ERD_HISPROJECT_REBUILD(MenuEnum.VERSION_MANAGE, 100030513200234547L, "erd_hisProject_rebuild", "重建基线"),
    //    ERD_HISPROJECT_EDIT(MenuEnum.VERSION_MANAGE, 100030513200234548L, "erd_hisProject_edit", "编辑"),
    //    ERD_HISPROJECT_DEL(MenuEnum.VERSION_MANAGE, 100030513200234549L, "erd_hisProject_del", "删除"),
    //    ERD_CONNECTOR_DBSYNC(MenuEnum.VERSION_MANAGE, 100030513200234550L, "erd_connector_dbsync", "同步数据源"),

    ERD_TABLE_IMPORT(MenuEnum.IMPORT, 100030513200234551L, "erd_table_import", "查看页面"),
    ERD_CONNECTOR_DBREVERSEPARSE(MenuEnum.IMPORT, 100030513200234552L, "erd_connector_dbReverseParse", "逆向解析"),
    ERD_TABLE_IMPORT_PDMAN(MenuEnum.IMPORT, 100030513200234553L, "erd_table_import_pdman", "PDMan"),
    ERD_TABLE_IMPORT_ERD(MenuEnum.IMPORT, 100030513200234554L, "erd_table_import_erd", "ERD"),

    ERD_TABLE_EXPORT(MenuEnum.EXPORT, 100030513200234555L, "erd_table_export", "查看页面"),
    ERD_TABLE_EXPORT_COMMON(MenuEnum.EXPORT, 100030513200234556L, "erd_table_export_common", "普通导出"),
    ERD_TABLE_EXPORT_MORE(MenuEnum.EXPORT, 100030513200234557L, "erd_table_export_more", "高级导出"),

    ERD_TABLE_SETTING(MenuEnum.SETUP, 100030513200234558L, "erd_table_setting", "查看页面"),
    ERD_TABLE_SETTING_DB(MenuEnum.SETUP, 100030513200234559L, "erd_table_setting_db", "数据源"),
    ERD_TABLE_SETTING_DEFAULTFIELD(MenuEnum.SETUP, 100030513200234560L, "erd_table_setting_defaultField", "默认字段"),
    ERD_TABLE_SETTING_DEFAULT(
        MenuEnum.SETUP,
        100030513200234561L,
        "erd_table_setting_default",
        "系统默认项"
    ),  //    ERD_DOC_DOWNLOADWORDTEMPLATE(MenuEnum.SETUP, 100030513200234562L, "erd_doc_downloadWordTemplate", "下载Word模板"),
    //    ERD_DOC_UPLOADWORDTEMPLATE(MenuEnum.SETUP, 100030513200234563L, "erd_doc_uploadWordTemplate", "上传Word模板"),

    ERD_SQL_EXECUTE(MenuEnum.QUERY, 100030513200234564L, "erd_sql_execute", "执行SQL"),
}
