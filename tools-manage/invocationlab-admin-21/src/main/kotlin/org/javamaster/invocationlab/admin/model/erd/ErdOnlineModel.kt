package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class ErdOnlineModel {
    var configJSON: ConfigJSONBean? = null
    var projectJSON: ProjectJSONBean? = null
    var projectName: String? = null
    var type: String? = null
    var id: String? = null
}
