package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import java.io.Serializable
import java.util.*

/**
 * @author yudong
 */

@AllOpen
class UsersVo : Serializable {
    var id: String? = null
    var username: String? = null
    var nickname: String? = null
    var gender: Int? = null
    var avatar: String? = null
    var blog: String? = null
    var company: String? = null
    var location: String? = null
    var email: String? = null
    var pwd: String? = null
    var salt: String? = null
    var age: Int? = null
    var signature: String? = null
    var title: String? = null
    var classification: String? = null
    var phone: String? = null
    var deptId: String? = null
    var deptName: String? = null
    var wechatOpenid: String? = null
    var qqOpenid: String? = null
    var tenantId: String? = null
    var lockFlag: Boolean? = null
    var delFlag: Boolean? = null
    var createTime: Date? = null
    var updateTime: Date? = null
    var creator: String? = null
    var updater: String? = null
}
