package org.javamaster.invocationlab.admin.service.creation

import org.javamaster.invocationlab.admin.service.GAV
import org.javamaster.invocationlab.admin.service.creation.entity.InterfaceEntity

/**
 * @author yudong
 */
interface PostmanService {
    fun getCluster(): String

    fun getServiceName(): String

    fun getGav(): GAV

    fun getInterfaceModels(): List<InterfaceEntity>

    fun getLoadedToClassLoader(): Boolean

    fun setLoadedToClassLoader(b: Boolean)
}
