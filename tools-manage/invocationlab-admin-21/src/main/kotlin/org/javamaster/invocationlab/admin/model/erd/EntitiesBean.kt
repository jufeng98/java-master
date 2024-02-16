package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class EntitiesBean {
    var title: String? = null
    var fields: List<FieldsBean>? = null
    var indexs: List<IndexsBean>? = null
    var headers: List<*>? = null
    var chnname: String? = null
    var originalCreateTableSql: String? = null
}
