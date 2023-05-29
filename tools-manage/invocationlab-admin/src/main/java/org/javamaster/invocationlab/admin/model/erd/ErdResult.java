package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

/**
 * @author yudong
 * @date 2023/2/12
 */
@Data
public class ErdResult<T> {
    private Integer code;
    private String msg;
    private T data;
}
