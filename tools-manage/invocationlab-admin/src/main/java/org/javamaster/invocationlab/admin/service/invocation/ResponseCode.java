package org.javamaster.invocationlab.admin.service.invocation;

/**
 * @author yudong
 */
public enum ResponseCode {
    SYSTEM_ERROR(-1, "系统错误"),
    APP_ERROR(-2, "访问应用错误");
    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
