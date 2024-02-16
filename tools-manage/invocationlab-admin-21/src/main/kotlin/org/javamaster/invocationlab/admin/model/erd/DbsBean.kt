package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/15
 */


@AllOpen
class DbsBean {
    var name: String? = null
    var select: String? = null
    var key: String? = null
    var defaultDB: Boolean? = null
    var properties: PropertiesBean? = null
}
