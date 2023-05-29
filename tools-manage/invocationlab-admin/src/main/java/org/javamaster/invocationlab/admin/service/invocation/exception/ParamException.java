package org.javamaster.invocationlab.admin.service.invocation.exception;

import org.javamaster.invocationlab.admin.service.invocation.ResponseCode;

/**
 * @author yudong
 * 参数解析异常
 */
public class ParamException extends Exception {

    private final int code;

    public ParamException(String msg) {
        super(msg);
        this.code = ResponseCode.SYSTEM_ERROR.getCode();
    }

    public int getCode() {
        return code;
    }
}
