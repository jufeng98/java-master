package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/15
 */


@AllOpen
class PropertiesBean {
    var driver_class_name: String? = null
    var url: String? = null
    var password: String? = null
    var username: String? = null
    var cipherType: String? = null
    var cipherKey: String? = null

    fun unique(): String {
        return "$url:$username:$password:$driver_class_name"
    }
}
