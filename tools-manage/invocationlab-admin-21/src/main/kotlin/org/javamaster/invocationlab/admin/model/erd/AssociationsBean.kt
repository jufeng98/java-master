package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class AssociationsBean {
    var relation: String? = null
    var from: FromBean? = null
    var to: ToBean? = null
}
