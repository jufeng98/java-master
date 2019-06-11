package org.javamaster.b2c.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author yudong
 * @date 2019/6/10
 */
public class Result<T> {
    private Boolean success;
    private Integer errorCode;
    private String errorMsg;
    private T data;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public Result(T data) {
        this(true, null, null, data);
    }

    public Result(Integer errorCode, String errorMsg) {
        this(false, errorCode, errorMsg, null);
    }

    public Result(Boolean success, Integer errorCode, String errorMsg, T data) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
