package org.javamaster.invocationlab.admin.model.redis

import org.javamaster.invocationlab.admin.annos.AllOpen


/**
 * @author yudong
 */
@AllOpen
class CommonRedisVo : ValueVo() {
    var connectId: String? = null
    var redisDbIndex: Int? = null

    var fieldKey: String? = null
    var fieldKeyJdkSerialize: Boolean? = null
    var score: Double? = null
    var oldRedisKey: String? = null
    var oldRedisValue: String? = null
}
