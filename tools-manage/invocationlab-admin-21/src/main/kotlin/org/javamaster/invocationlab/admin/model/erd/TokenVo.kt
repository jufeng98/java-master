package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable


@AllOpen
class TokenVo : Serializable {
    @JsonProperty("access_token")
    var accessToken: String? = null

    @JsonProperty("token_type")
    var tokenType: String? = null

    @JsonProperty("refresh_token")
    var refreshToken: String? = null

    @JsonProperty("expires_in")
    var expiresIn = 0
    var scope: String? = null

    @JsonProperty("tenant_id")
    var tenantId: String? = null
    var license: String? = null

    @JsonProperty("dept_id")
    var deptId: String? = null

    @JsonProperty("dept_name")
    var deptName: String? = null

    @JsonProperty("user_id")
    var userId: String? = null
    var username: String? = null
    var email: String? = null
    var mobileNo: String? = null
    var env: String? = null
}
