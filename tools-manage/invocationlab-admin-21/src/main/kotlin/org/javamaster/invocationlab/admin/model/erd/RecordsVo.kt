package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.*

/**
 * @author yudong
 * @date 2023/2/14
 */


@AllOpen
class RecordsVo {
    var id: String? = null
    var projectName: String? = null
    var tags: String? = null
    var description: String? = null
    var type: Int? = null
    var creator: String? = null

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var createTime: Date? = null
    var updater: String? = null

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var updateTime: Date? = null
}
