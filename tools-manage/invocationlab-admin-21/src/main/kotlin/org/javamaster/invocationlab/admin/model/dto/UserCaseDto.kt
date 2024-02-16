package org.javamaster.invocationlab.admin.model.dto

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * 用例详细信息
 *
 * @author yudong
 */

@AllOpen
class UserCaseDto : AbstractCaseDto() {
    var zkAddress: String? = null
    var serviceName: String? = null

    /**
     * 类路径,包含package和版本
     */
    var interfaceKey: String? = null

    /**
     * 接口的简单名称,eg:QueryProvider
     */
    var className: String? = null
    var methodName: String? = null
    var requestValue: String? = null
    var responseValue: String? = null
    var testScript: String? = null
}
