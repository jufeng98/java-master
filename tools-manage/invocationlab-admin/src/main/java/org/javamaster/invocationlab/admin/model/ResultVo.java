package org.javamaster.invocationlab.admin.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yudong
 * @date 2023/2/14
 */
@Data
@AllArgsConstructor
public class ResultVo<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> ResultVo<T> success(T data) {
        return new ResultVo<>(200, "操作成功", data);
    }

    public static <T> ResultVo<T> fail(String msg) {
        return new ResultVo<>(400, msg, null);
    }

    public static <T> ResultVo<T> fail(Integer code, String msg) {
        return new ResultVo<>(code, msg, null);
    }
}
