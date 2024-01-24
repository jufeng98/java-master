package org.javamaster.invocationlab.admin.model.dto;

import lombok.Getter;

import java.util.Objects;

/**
 * powered by WebApiResponse
 *
 * @param <T>
 * @author yudong
 */
public class WebApiRspDto<T> {
    public static final int SUCCESS_CODE = 0;
    public static final int ERROR_CODE = 1;
    @Getter
    private int code;
    @Getter
    private String error;
    @Getter
    private String msg;
    @Getter
    private T data;
    @Getter
    private long elapse;
    /**
     * 是否需要重试。只有在 code != 0，也就是说有错的时候才有意义。
     * 为真时表示一定要重试，为假时表示一定不要重试，为空时由调用方自行决定。
     */
    private Boolean isNeedRetry;

    public static <T> WebApiRspDto<T> success(T data) {
        WebApiRspDto<T> response = new WebApiRspDto<>();
        response.setCode(SUCCESS_CODE);
        response.setData(data);
        return response;
    }

    public static <T> WebApiRspDto<T> success(T data, long elapse) {
        WebApiRspDto<T> response = new WebApiRspDto<>();
        response.setCode(SUCCESS_CODE);
        response.setData(data);
        response.setElapse(elapse);
        return response;
    }

    public static <T> WebApiRspDto<T> error(String errorMessage) {
        return WebApiRspDto.error(errorMessage, ERROR_CODE);
    }

    public static <T> WebApiRspDto<T> error(String errorMessage, int errorCode) {
        return WebApiRspDto.error(errorMessage, errorCode, null);
    }

    public static <T> WebApiRspDto<T> error(String errorMessage, Boolean isNeedRetry) {
        return WebApiRspDto.error(errorMessage, ERROR_CODE, isNeedRetry);
    }

    public static <T> WebApiRspDto<T> error(String errorMessage, int errorCode, Boolean isNeedRetry) {
        WebApiRspDto<T> response = new WebApiRspDto<>();
        response.setCode(errorCode);
        response.setError(errorMessage);
        response.setNeedRetry(isNeedRetry);
        return response;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Boolean getNeedRetry() {
        return isNeedRetry;
    }

    public void setNeedRetry(Boolean needRetry) {
        isNeedRetry = needRetry;
    }

    public void setElapse(long elapse) {
        this.elapse = elapse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebApiRspDto<?> that = (WebApiRspDto<?>) o;
        if (code != that.code) {
            return false;
        }
        if (!Objects.equals(error, that.error)) {
            return false;
        }
        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + (error != null ? error.hashCode() : 0);
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "WebApiResponse{" +
                "code=" + code +
                "elapse=" + elapse +
                ", error='" + error + '\'' +
                ", data=" + data +
                '}';
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
