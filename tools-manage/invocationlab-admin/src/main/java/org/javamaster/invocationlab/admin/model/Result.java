package org.javamaster.invocationlab.admin.model;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = -490238572284858235L;
    private Boolean isSuccess;
    private Integer responseCode;
    private String responseMsg;
    private T data;
    private Long count;

    public Result() {
    }

    public Result(T data) {
        this(true, 0, "请求成功", data, null);
    }

    public Result(Pair<T, Long> pair) {
        this(true, 0, "请求成功", pair.getLeft(), pair.getRight());
    }

    public Result(T data, Long count) {
        this(true, 0, "请求成功", data, count);
    }

    public Result(Integer responseCode, String responseMsg) {
        this(false, responseCode, responseMsg, null, null);

    }

    public Result(Boolean isSuccess, Integer responseCode, String responseMsg) {
        this(isSuccess, responseCode, responseMsg, null, null);
    }

    public Result(Boolean isSuccess, Integer responseCode, String responseMsg, T data, Long count) {
        this.isSuccess = isSuccess;
        this.responseCode = responseCode;
        this.responseMsg = responseMsg;
        this.data = data;
        this.count = count;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    public static <T> Result<T> success(T data, long count) {
        return new Result<>(data, count);
    }

    public static <T> Result<T> fail(Integer responseCode, String responseMsg) {
        return new Result<>(responseCode, responseMsg);
    }

}
