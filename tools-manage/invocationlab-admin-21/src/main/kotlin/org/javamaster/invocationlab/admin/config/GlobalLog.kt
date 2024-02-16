package org.javamaster.invocationlab.admin.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GlobalLog {
    companion object {
        @Suppress("UnusedReceiverParameter")
        val <reified T> T.log: Logger
            inline get() = LoggerFactory.getLogger(T::class.java)
    }
}