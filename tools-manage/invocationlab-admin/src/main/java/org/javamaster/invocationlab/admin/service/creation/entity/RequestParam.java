package org.javamaster.invocationlab.admin.service.creation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yudong
 * 定义参数的匹配关系
 */
@Data
public class RequestParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String paraName;

    @JsonIgnore
    private Class<?> targetParaType;
}
