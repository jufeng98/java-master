package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen


@AllOpen
class SortModuleReqVo {
    var projectId: String? = null
    var sortModuleVos: List<SortModuleVo>? = null
}
