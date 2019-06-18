package org.javamaster.b2c.test.validation;

/**
 * @author yudong
 * @date 2019/6/17
 */
public enum CarTypeEnum {

    UNKNOWN(0, ""),
    BMW(1, "宝马"),
    AO_DI(2, "奥迪"),
    BENZ(3, "奔驰");
    private final int code;
    private final String msg;

    CarTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
