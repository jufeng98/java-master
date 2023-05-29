package org.javamaster.invocationlab.admin.enums;

/**
 * @author yudong
 * @date 2023/4/27
 */
public enum MenuEnum {
    TEAM_BASE_SETUP("团队基本设置"),
    TEAM_PERMISSION_GROUP("团队权限组"),
    MODEL_DESIGN("模型设计"),
    IMPORT("导入"),
    EXPORT("导出"),
    SETUP("设置"),
    QUERY("查询"),
    ;

    public final String menuName;

    MenuEnum(String menuName) {
        this.menuName = menuName;
    }
}
