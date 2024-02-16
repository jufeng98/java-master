package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class DatatypeBean {
    var name: String? = null
    var code: String? = null
    var apply: ApplyBean? = null
}
