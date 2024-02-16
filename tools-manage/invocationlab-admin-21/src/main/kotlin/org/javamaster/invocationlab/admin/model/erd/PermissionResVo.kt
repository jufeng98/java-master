package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable


@AllOpen
class PermissionResVo : Serializable {
    var loginRole: Int? = null
    var checkboxes: List<CheckboxesVo>? = null
}
