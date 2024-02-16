package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable


@AllOpen
class SaveCheckedOperationsReqVo : Serializable {
    var roleId: String? = null
    var projectId: String? = null
    var checkedKeys: Set<Long>? = null
}
