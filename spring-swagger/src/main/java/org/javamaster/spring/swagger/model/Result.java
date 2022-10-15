package org.javamaster.spring.swagger.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @param <T>
 * @author yudong
 * @date 2022/1/4
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = -490238572284858235L;
    private Boolean isSuccess;
    private Integer responseCode;
    private String responseMsg;
    private T data;

    public Result() {
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    public static <T> Result<T> fail(Integer responseCode, String responseMsg) {
        return new Result<>(responseCode, responseMsg);
    }

    private Result(T data) {
        this(true, 0, "请求成功", data);
    }

    private Result(Integer responseCode, String responseMsg) {
        this(false, responseCode, responseMsg, null);

    }

    private Result(Boolean isSuccess, Integer responseCode, String responseMsg, T data) {
        this.isSuccess = isSuccess;
        this.responseCode = responseCode;
        this.responseMsg = responseMsg;
        this.data = data;
    }

}
