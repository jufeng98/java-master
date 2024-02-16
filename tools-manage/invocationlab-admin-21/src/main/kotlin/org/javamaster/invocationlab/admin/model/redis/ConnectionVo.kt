package org.javamaster.invocationlab.admin.model.redis

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 */


@AllOpen
class ConnectionVo {
    var connectId: String? = null
    var nodes: String? = null
    var name: String? = null
    var host: String? = null
    var port: Int? = null
    var user: String? = null
    var password: String? = null
    var createTime: Long? = null
}
