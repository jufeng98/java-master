package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable


@AllOpen
class OperationsVo : Serializable {
    var name: String? = null
    var value: Long? = null
}
