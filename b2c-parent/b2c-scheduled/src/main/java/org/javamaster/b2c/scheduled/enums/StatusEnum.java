package org.javamaster.b2c.scheduled.enums;

/**
 * @author yudong
 * @date 2019/8/24
 */
public enum StatusEnum {

    ENABLED(1),
    DISABLED(2),
    ;

    private Integer code;

    StatusEnum(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
