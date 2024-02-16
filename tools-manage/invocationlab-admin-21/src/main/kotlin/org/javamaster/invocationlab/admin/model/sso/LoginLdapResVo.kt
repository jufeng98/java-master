package org.javamaster.invocationlab.admin.model.sso

import org.javamaster.invocationlab.admin.annos.AllOpen


@AllOpen
class LoginLdapResVo {
    var refreshToken: String? = null
    var email: String? = null
    var account: String? = null
    var realName: String? = null
    var token: String? = null
    var blood: String? = null
    var mobileNo: String? = null
    var accountType: Int? = null
    var sex: String? = null
    var empType: String? = null
}
