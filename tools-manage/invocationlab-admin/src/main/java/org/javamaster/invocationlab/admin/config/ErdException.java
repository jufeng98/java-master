package org.javamaster.invocationlab.admin.config;

/**
 * @author yudong
 * @date 2023/2/16
 */
public class ErdException extends RuntimeException {
    private static final long serialVersionUID = 2362669725006014925L;
    private int code = 400;

    public ErdException(int code, String message) {
        this(message);
        this.code = code;
    }

    public ErdException(String message) {
        super(message);
    }

    public int getCode() {
        return code;
    }
}
