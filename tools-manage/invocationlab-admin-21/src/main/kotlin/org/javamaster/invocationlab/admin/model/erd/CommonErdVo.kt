package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import com.alibaba.fastjson.JSONArray


@AllOpen
class CommonErdVo {
    var projectId: String? = null
    var selectDB: String? = null
    var tableName: String? = null
    var sql: String? = null
    var queryId: String? = null
    var type: String? = null

    var explain: Boolean? = null

    var page: Int? = null
    var pageSize: Int? = null

    var isExport: Boolean? = null

    var rows: JSONArray? = null
}
