package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class EdgesBean {
    var shape: String? = null
    var relation: String? = null
    var source: String? = null
    var target: String? = null
    var id: String? = null
    var controlPoints: List<ControlPointsBean>? = null
    var sourceAnchor: Int? = null
    var targetAnchor: Int? = null
}
