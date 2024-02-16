package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import com.fasterxml.jackson.databind.JsonNode


@AllOpen
class SaveProjectVo {
    var erdOnlineModel: ErdOnlineModel? = null
    var delta: JsonNode? = null
}
