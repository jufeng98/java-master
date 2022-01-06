package org.javamaster.spring.swagger.enums;

/**
 * @author yudong
 * @date 2022/1/4
 */
public enum SexEnum implements EnumBase {
    MAN(1, "男"),
    WOMAN(2, "女"),
    ;

    public final Integer code;
    public final String name;

    SexEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getText() {
        return name;
    }
}
