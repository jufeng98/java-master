package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable


@AllOpen
class RolePermissionResVo : Serializable {
    var loginRole: Int? = null
    var permission: List<String>? = null
}
