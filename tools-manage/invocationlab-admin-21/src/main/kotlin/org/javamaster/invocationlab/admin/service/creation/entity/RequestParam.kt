package org.javamaster.invocationlab.admin.service.creation.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

/**
 * @author yudong
 * 定义参数的匹配关系
 */

class RequestParam : Serializable {
    var paraName: String? = null

    @JsonIgnore
    var targetParaType: Class<*>? = null
}
