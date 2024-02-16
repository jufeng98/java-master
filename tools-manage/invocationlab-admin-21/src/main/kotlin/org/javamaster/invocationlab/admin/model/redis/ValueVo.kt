package org.javamaster.invocationlab.admin.model.redis

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 */


@AllOpen
class ValueVo {
    var redisValue: String? = null
    var redisValueSize: Int? = null
    var redisValueJdkSerialize: Boolean? = null
    var redisValueClazz: Class<*>? = null
    var redisKeyBase64: String? = null

    var redisKeyTtl: Long? = null
    var redisKey: String? = null
    var redisKeyClazz: Class<*>? = null
    var redisKeyJdkSerialize: Boolean? = null
    var redisKeyType: String? = null

    var fieldVos: List<FieldVo>? = null
    var fieldCount: Int? = null
}
