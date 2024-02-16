package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class IndexsBean {
    var name: String? = null
    var fields: List<String>? = null
    var isUnique: Boolean? = null
}
