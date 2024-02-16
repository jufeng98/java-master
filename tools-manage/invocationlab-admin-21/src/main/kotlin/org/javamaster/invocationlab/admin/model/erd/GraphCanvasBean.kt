package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class GraphCanvasBean {
    var nodes: List<NodesBean>? = null
    var edges: List<EdgesBean>? = null
}
