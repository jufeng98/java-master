package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */

@AllOpen
class ErdResult<T> {
    var code: Int? = null
    var msg: String? = null
    var data: T? = null
}
