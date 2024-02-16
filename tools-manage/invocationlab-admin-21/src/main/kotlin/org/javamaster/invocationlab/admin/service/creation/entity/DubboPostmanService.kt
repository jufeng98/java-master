package org.javamaster.invocationlab.admin.service.creation.entity

import org.javamaster.invocationlab.admin.service.GAV
import org.javamaster.invocationlab.admin.service.creation.PostmanService
import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * @author yudong
 */

class DubboPostmanService :
    PostmanService {
    @JvmField
    var cluster: String? = null

    @JvmField
    var serviceName: String? = null

    @JvmField
    var gav: GAV = GAV()

    @JvmField
    var generateTime: Long = 0

    /**
     * 标识是否加载到classLoader
     * 这个值不能持久化
     */
    @JsonIgnore
    @JvmField
    var loadedToClassLoader: Boolean = false

    /**
     * 一个dubbo应用包含多个接口定义
     */
    @JvmField
    var interfaceModels: MutableList<InterfaceEntity> = mutableListOf()

    override fun getCluster(): String {
        return cluster!!
    }

    override fun getServiceName(): String {
        return serviceName!!
    }

    override fun getGav(): GAV {
        return gav
    }

    fun getGenerateTime(): Long {
        return this.generateTime
    }

    override fun getInterfaceModels(): MutableList<InterfaceEntity> {
        return interfaceModels
    }

    override fun getLoadedToClassLoader(): Boolean {
        return loadedToClassLoader
    }

    override fun setLoadedToClassLoader(b: Boolean) {
        loadedToClassLoader = b
    }

    fun setCluster(cluster: String?) {
        this.cluster = cluster
    }

    fun setServiceName(serviceName: String?) {
        this.serviceName = serviceName
    }

    fun setGav(gav: GAV?) {
        this.gav = gav!!
    }

    fun setGenerateTime(generateTime: Long) {
        this.generateTime = generateTime
    }

    fun setInterfaceModels(interfaceModels: MutableList<InterfaceEntity>) {
        this.interfaceModels = interfaceModels
    }
}
