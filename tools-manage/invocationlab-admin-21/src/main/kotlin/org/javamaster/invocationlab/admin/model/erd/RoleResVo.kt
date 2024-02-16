package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable


@AllOpen
class RoleResVo : Serializable {
    var id: Long? = null
    var roleId: String? = null
    var projectId: String? = null
    var roleName: String? = null
    var roleCode: String? = null
}
