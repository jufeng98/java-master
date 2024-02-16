package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class ModulesBean {
    var name: String? = null
    var chnname: String? = null
    var sort: Int? = null
    var entities: List<EntitiesBean>? = null
    var graphCanvas: GraphCanvasBean? = null
    var associations: List<AssociationsBean>? = null
}
