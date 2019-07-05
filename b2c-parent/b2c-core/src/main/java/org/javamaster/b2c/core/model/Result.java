package org.javamaster.b2c.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author yudong
 * @date 2019/6/10
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Result<T> {
    private Boolean success;
    private Integer errorCode;
    private String errorMsg;
    private T data;
    private Long total;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public Result(T data) {
        this(true, null, null, data, null);
    }

    public Result(Integer errorCode, String errorMsg) {
        this(false, errorCode, errorMsg, null, null);
    }

    public Result(T data, Long total) {
        this.data = data;
        this.total = total;
    }

    public Result(Boolean success, Integer errorCode, String errorMsg, T data, Long total) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.data = data;
        this.total = total;
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

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
