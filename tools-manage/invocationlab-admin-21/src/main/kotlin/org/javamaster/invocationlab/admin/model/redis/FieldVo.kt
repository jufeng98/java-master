package org.javamaster.invocationlab.admin.model.redis

import org.javamaster.invocationlab.admin.annos.AllOpen


/**
 * @author yudong
 */
@AllOpen
class FieldVo {
    var fieldIndex: Int? = null
    var fieldKey: String? = null
    var fieldKeyClazz: Class<*>? = null
    var fieldKeyJdkSerialize: Boolean? = null

    var fieldValue: String? = null
    var fieldValueJdkSerialize: Boolean? = null
    var fieldValueClazz: Class<*>? = null
    var fieldScore: Double? = null
    var fieldValueSize: Int? = null
}
