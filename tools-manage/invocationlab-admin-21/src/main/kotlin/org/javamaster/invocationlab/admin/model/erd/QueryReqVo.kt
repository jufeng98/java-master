package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen


@AllOpen
class QueryReqVo {
    var projectId: String? = null
    var isLeaf: Boolean? = null
    var title: String? = null
    var parentId: String? = null
}
