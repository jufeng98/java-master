package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.util.*

@AllOpen
class ExecuteHistoryBean {
    var sqlInfo: String? = null
    var dbName: String? = null
    var duration: Long? = null
    var createTime: Date? = null
    var creator: String? = null
    var params: String? = null
}
