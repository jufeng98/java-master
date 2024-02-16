package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable


@AllOpen
class CheckboxesVo : Serializable {
    var menuName: String? = null
    var menuId: String? = null
    var defaultValue: List<Long>? = null
    var operations: List<OperationsVo>? = null
}
