package org.javamaster.invocationlab.admin.enums;

import java.util.Objects;

public enum CipherTypeEnum {
    AES("AES"),
    MYSQL_AES("MYSQL-AES"),
    ;

    public final String type;

    CipherTypeEnum(String type) {
        this.type = type;
    }

    public static CipherTypeEnum getByType(String type) {
        for (CipherTypeEnum value : CipherTypeEnum.values()) {
            if (Objects.equals(value.type, type)) {
                return value;
            }
        }
        throw new IllegalArgumentException("wrong type:" + type);
    }
}
