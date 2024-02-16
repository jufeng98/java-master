package org.javamaster.invocationlab.admin.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */

object FileUtils {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @JvmStatic
    fun readStringFromUrl(url: URL): String {
        val urlConnection = url.openConnection()
        urlConnection.useCaches = false
        urlConnection.getInputStream().use {
            val len = it.available()
            val data = ByteArray(len)
            val read = it.read(data, 0, len)
            log.info("read url:{} length:{}", url, read)
            return String(data, StandardCharsets.UTF_8)
        }
    }
}
