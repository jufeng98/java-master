package org.javamaster.invocationlab.admin.util

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * @author yudong
 */
@Component
class SpringUtils : ApplicationContextAware {
    override fun setApplicationContext(ac: ApplicationContext) {
        context = ac
    }

    companion object {
        lateinit var context: ApplicationContext

        var proEnv = false

        var devEnv = false

    }
}
