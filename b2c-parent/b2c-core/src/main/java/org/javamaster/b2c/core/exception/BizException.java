package org.javamaster.b2c.core.exception;

import org.javamaster.b2c.core.enums.BizExceptionEnum;

/**
 * @author yudong
 * @date 2019/6/10
 */
public class BizException extends RuntimeException {
    private final BizExceptionEnum bizExceptionEnum;

    public BizException(BizExceptionEnum bizExceptionEnum) {
        super(bizExceptionEnum.getErrorMsg());
        this.bizExceptionEnum = bizExceptionEnum;
    }

    public BizExceptionEnum getBizExceptionEnum() {
        return bizExceptionEnum;
    }
}
