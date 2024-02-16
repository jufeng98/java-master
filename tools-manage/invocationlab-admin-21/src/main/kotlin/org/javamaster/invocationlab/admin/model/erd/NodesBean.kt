package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class NodesBean {
    var shape: String? = null
    var title: String? = null
    var moduleName: Boolean? = null
    var x: Int? = null
    var y: Int? = null
    var id: String? = null
}
