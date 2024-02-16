package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable
import java.util.*


@AllOpen
class GroupGetVo : Serializable {
    var id: String? = null
    var configJSON: String? = null
    var projectJSON: String? = null
    var projectName: String? = null
    var description: String? = null
    var type: String? = null
    var tags: String? = null
    var revision: Any? = null
    var delFlag: String? = null
    var creator: String? = null
    var createTime: Date? = null
    var updater: String? = null
    var updateTime: Date? = null
}
