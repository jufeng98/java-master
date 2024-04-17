package org.javamaster.invocationlab.admin.enums;

/**
 * @author yudong
 * @date 2023/9/3
 */
public enum SqlTypeEnum {
    SELECT("select"),
    DELETE("delete"),
    UPDATE("update"),
    INSERT("insert"),
    CREATE("create"),
    DROP("drop"),
    ALTER("alter"),
    ;

    public final String type;

    SqlTypeEnum(String type) {
        this.type = type;
    }

    public static SqlTypeEnum getByType(String type) {
        for (SqlTypeEnum value : SqlTypeEnum.values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        throw new IllegalArgumentException(type);
    }
}
