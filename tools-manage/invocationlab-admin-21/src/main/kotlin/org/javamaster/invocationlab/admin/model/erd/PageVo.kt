package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import com.alibaba.fastjson.JSONObject

/**
 * @author yudong
 * @date 2023/2/14
 */
@AllOpen
class PageVo {
    var records: MutableList<RecordsVo>? = null
    var total: Int? = null
    var size: Int? = null
    var current: Int? = null
    var orders: List<JSONObject>? = null
    var searchCount: Boolean? = null
    var pages: Int? = null
}
