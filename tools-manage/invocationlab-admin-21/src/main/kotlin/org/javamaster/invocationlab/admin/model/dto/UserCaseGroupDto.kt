package org.javamaster.invocationlab.admin.model.dto

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * 用例分组
 *
 * @author yudong
 */

@AllOpen
class UserCaseGroupDto {
    var value: String? = null
    var label: String? = null
    var children: List<UserCaseGroupDto> = ArrayList()
}
