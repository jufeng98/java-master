package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable


@AllOpen
class AesReqVo : Serializable {
    var projectId: String? = null
    var opType: String? = null
    var value: String? = null
}
