package org.javamaster.invocationlab.admin.model.dto

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * 测试场景详细信息标识
 *
 * @author yudong
 */

@AllOpen
class SceneCaseDto {
    var caseName: String? = null
    var caseDtoList: List<UserCaseDto> = ArrayList()
    var sceneScript: String? = null
}
