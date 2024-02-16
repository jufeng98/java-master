package org.javamaster.invocationlab.admin.service.creation.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import java.lang.reflect.Method

/**
 * @author yudong
 * 一个方法唯一标识一个访问路径,path通常是一个全路径信息
 */

class MethodEntity {
    /**
     * 包含参数的全名称
     * eg:test(int,Object)
     */
    var name: String? = null

    @JsonIgnore
    var method: Method? = null

    var params: List<ParamEntity> = mutableListOf()
}
