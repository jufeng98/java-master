package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class ProjectJSONBean {
    var modules: MutableList<ModulesBean>? = null
    var profile: ProfileBean? = null
    var dataTypeDomains: DataTypeDomainsBean? = null
}
