package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class DatabaseBean {
    var code: String? = null
    var template: String? = null
    var fileShow: Boolean? = null
    var defaultDatabase: Boolean? = null
    var createTableTemplate: String? = null
    var deleteTableTemplate: String? = null
    var rebuildTableTemplate: String? = null
    var createFieldTemplate: String? = null
    var updateFieldTemplate: String? = null
    var deleteFieldTemplate: String? = null
    var deleteIndexTemplate: String? = null
    var createIndexTemplate: String? = null
    var updateTableComment: String? = null
}
