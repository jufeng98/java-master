package org.javamaster.b2c.scheduled.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.javamaster.b2c.scheduled.consts.AppConsts;

/**
 * @author yudong
 * @date 2020/7/4
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;
    private Long total;

    public Result(Integer code, String msg) {
        this(code, msg, null, null);
    }

    public Result(T data) {
        this(AppConsts.SUCCESS, AppConsts.SUCCESS_MSG, data, null);
    }

    public Result(T data, Long total) {
        this(AppConsts.SUCCESS, AppConsts.SUCCESS_MSG, data, total);
    }

    public Result(Integer code, String msg, T data, Long total) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.total = total;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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
