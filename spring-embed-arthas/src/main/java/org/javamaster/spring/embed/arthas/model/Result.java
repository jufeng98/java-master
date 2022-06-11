package org.javamaster.spring.embed.arthas.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author yudong
 * @date 2022/6/4
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Result<T> {
    private Boolean success;
    private Integer errorCode;
    private String errorMsg;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(data);
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

}
