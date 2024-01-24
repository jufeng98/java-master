package org.javamaster.invocationlab.admin.config;

import lombok.Getter;

/**
 * @author yudong
 * @date 2023/2/16
 */
@Getter
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

}
