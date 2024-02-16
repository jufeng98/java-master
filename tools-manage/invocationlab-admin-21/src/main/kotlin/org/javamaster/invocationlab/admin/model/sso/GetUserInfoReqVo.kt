package org.javamaster.invocationlab.admin.model.sso

import org.javamaster.invocationlab.admin.annos.AllOpen


@AllOpen
class GetUserInfoReqVo {
    var account: String? = null
    var appType: String? = null
    var accountType: String? = null
}
