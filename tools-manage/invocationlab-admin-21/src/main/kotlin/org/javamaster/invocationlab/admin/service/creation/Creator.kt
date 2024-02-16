package org.javamaster.invocationlab.admin.service.creation

import org.javamaster.invocationlab.admin.service.GAV
import org.javamaster.invocationlab.admin.service.Pair

/**
 * @author yudong
 */
interface Creator {
    fun create(cluster: String, gav: GAV, serviceName: String): Pair<Boolean, String>

    fun remove(cluster: String, serviceName: String)

    fun refresh(cluster: String, serviceName: String): Pair<Boolean, String>

    fun getLatestVersion(gav: GAV): String
}
