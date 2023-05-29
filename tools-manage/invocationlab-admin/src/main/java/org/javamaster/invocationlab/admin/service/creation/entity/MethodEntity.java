package org.javamaster.invocationlab.admin.service.creation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 * 一个方法唯一标识一个访问路径,path通常是一个全路径信息
 */
@Data
public class MethodEntity {

    /**
     * 包含参数的全名称
     * eg:test(int,Object)
     */
    String name;

    @JsonIgnore
    Method method;

    List<ParamEntity> params = new ArrayList<>();
}
