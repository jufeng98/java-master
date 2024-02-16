package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen


@AllOpen
class SqlExecResVo {
    var columns: Set<String>? = null
    var tableColumns: Map<String, Column>? = null

    var queryKey: Int? = null

    var page: Int? = null
    var pageSize: Int? = null

    var showPagination: Boolean? = null
    var tableName: String? = null
    var primaryKeys: List<String>? = null

    var tableData: TableData? = null
}
