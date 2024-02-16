package org.javamaster.invocationlab.admin.service.invocation

import org.javamaster.invocationlab.admin.service.invocation.entity.DubboParamValue
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author yudong
 * @date 2022/11/13
 */
open class AbstractInvoker {
    @JvmField
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    protected lateinit var converter: Converter<PostmanDubboRequest, DubboParamValue>
}
