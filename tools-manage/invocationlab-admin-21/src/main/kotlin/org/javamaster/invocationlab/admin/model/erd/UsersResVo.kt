package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable

/**
 * @author yudong
 */

@AllOpen
class UsersResVo : Serializable {
    var total: Int? = null
    var size: Int? = null
    var current: Int? = null
    var searchCount = false
    var pages: Int? = null
    var records: List<UsersVo>? = null
    var orders: List<Any>? = null
}
