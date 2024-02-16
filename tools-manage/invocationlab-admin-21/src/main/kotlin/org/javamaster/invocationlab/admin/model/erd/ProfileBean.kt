package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class ProfileBean {
    var defaultFields: List<DefaultFieldsBean>? = null
    var dbs: List<DbsBean>? = null
}
