package org.javamaster.invocationlab.admin.model.dto

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * 标识一个测试用例
 *
 * @author yudong
 */

@AllOpen
open class AbstractCaseDto {
    var groupName: String?
    var caseName: String?

    constructor() : this("", "")

    constructor(groupName: String?, caseName: String?) {
        this.groupName = groupName
        this.caseName = caseName
    }
}
