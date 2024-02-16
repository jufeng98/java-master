package org.javamaster.invocationlab.admin.service.registry

/**
 * @author yudong
 */
interface RegisterFactory {
    fun addCluster(cluster: String)

    fun remove(cluster: String): Register

    fun get(cluster: String): Register

    fun refreshService(interfaceNames: List<String>, cluster: String) {}

    fun getClusterSet(): MutableSet<String>
}
