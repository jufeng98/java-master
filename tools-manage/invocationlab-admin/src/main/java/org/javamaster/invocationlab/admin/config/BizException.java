package org.javamaster.invocationlab.admin.config;

/**
 * @author yudong
 * @date 2023/12/30
 */
public class BizException extends RuntimeException {
    private static final long serialVersionUID = 2362669225006014925L;

    public BizException(String message) {
        super(message);
    }

}
