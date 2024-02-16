package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2019/7/8
 */

@AllOpen
class Table(var name: String? = null, var remarks: String? = null) {

    constructor() : this("", "")

    override fun toString(): String {
        return "$name  $remarks"
    }
}
