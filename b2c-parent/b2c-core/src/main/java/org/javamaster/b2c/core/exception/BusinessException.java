package org.javamaster.b2c.core.exception;

/**
 * 业务异常类
 *
 * @author yudong
 * @date 2020/6/19
 */
public class BusinessException extends RuntimeException {
    private int errorCode;

    public BusinessException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
