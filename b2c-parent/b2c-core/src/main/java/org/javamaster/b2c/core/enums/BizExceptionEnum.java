package org.javamaster.b2c.core.enums;

/**
 * @author yudong
 * @date 2019/6/10
 */
public enum BizExceptionEnum {
    APPLICATION_ERROR(1000, "网络繁忙,请稍后再试"),
    INVALID_USER(1001, "用户名或密码错误"),
    INVALID_REQ_PARAM(1002, "参数错误"),
    USERNAME_NOT_EXISTS(1003, "用户不存在"),
    PASSWORD_INCORRECT(1004, "密码错误"),
    EXAM_NOT_FOUND(2001, "未查到考试信息"),
    ;

    BizExceptionEnum(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    private final Integer errorCode;
    private final String errorMsg;

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
