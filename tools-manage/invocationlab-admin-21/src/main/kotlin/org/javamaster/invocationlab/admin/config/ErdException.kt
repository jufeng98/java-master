package org.javamaster.invocationlab.admin.config

/**
 * @author yudong
 * @date 2023/2/16
 */

class ErdException(message: String) : RuntimeException(message) {
    var code = 400

    constructor(code: Int, message: String) : this(message) {
        this.code = code
    }
}
