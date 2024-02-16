package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen


@AllOpen
class SaveQueryReqVo {
    var projectId: String? = null
    var sqlInfo: String? = null
    var selectDB: String? = null
    var title: String? = null
    var treeNodeId: String? = null
}
