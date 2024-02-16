package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class FieldsBean {
    var chnname: String? = null
    var name: String? = null
    var typeName: String? = null
    var type: String? = null
    var dataType: String? = null
    var rawDataType: String? = null
    var remark: String? = null
    var pk: Boolean? = null
    var notNull: Boolean? = null
    var autoIncrement: Boolean? = null
    var relationNoShow: Boolean? = null
    var defaultValue: String? = null
    var uiHint: String? = null
}
