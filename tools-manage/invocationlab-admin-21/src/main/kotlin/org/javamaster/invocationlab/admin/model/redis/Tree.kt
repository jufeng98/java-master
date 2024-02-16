package org.javamaster.invocationlab.admin.model.redis

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 */


@AllOpen
class Tree {
    var redisDbIndex: Int? = null
    var keyCount: Long? = null
    var label: String? = null
    var labelBase64: String? = null
    var isLeaf: Boolean? = null
}
