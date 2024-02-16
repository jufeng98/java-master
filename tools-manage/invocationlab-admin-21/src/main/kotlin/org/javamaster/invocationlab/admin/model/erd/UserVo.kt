package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable

/**
 * @author yudong
 */

@AllOpen
class UserVo : Serializable {
    var orgId: String? = null
    var orgName: String? = null
    var roleIds: Set<String>? = null
    var password: String? = null
}
