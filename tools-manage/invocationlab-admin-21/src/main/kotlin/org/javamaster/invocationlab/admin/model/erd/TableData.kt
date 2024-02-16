package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen


@AllOpen
class TableData {
    var total: Int? = null
    var realTotal: Int? = null
    var records: List<MutableMap<String, Any>>? = null
}
