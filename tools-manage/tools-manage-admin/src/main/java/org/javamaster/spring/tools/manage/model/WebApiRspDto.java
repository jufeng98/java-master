package org.javamaster.spring.tools.manage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yudong
 * @date 2023/3/3
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class WebApiRspDto<T> {
    public static final int SUCCESS_CODE = 0;
    public static final int ERROR_CODE = 1;
    private int code;
    private String msg;
    private T data;

    public static <T> WebApiRspDto<T> success(T data) {
        WebApiRspDto<T> response = new WebApiRspDto<>();
        response.setCode(SUCCESS_CODE);
        response.setData(data);
        return response;
    }

    public static <T> WebApiRspDto<T> error(String errorMessage) {
        return WebApiRspDto.error(errorMessage, ERROR_CODE);
    }

    public static <T> WebApiRspDto<T> error(String errorMessage, int errorCode) {
        return new WebApiRspDto<>(errorCode, errorMessage, null);
    }

}
