package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class DataTypeDomainsBean {
    var datatype: List<DatatypeBean>? = null
    var database: List<DatabaseBean>? = null
}
