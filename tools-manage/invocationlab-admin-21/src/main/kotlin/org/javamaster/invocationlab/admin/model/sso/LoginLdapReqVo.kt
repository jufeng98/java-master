package org.javamaster.invocationlab.admin.model.sso

import org.javamaster.invocationlab.admin.annos.AllOpen


@AllOpen
class LoginLdapReqVo {
    var account: String? = null
    var pwdSwitch: Int? = null
    var appType: String? = null
    var clientType: String? = null
    var password: String? = null
    var deviceNum: String? = null
    var manuFacturer: String? = null
    var accountType: Int? = null
}
