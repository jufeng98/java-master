package org.javamaster.invocationlab.admin.service.registry.impl

import org.javamaster.invocationlab.admin.service.registry.Register
import org.javamaster.invocationlab.admin.service.registry.RegisterFactory

/**
 * @author yudong
 */
abstract class AbstractRegisterFactory : RegisterFactory {
    private var allRegisters: MutableMap<String, Register> = HashMap()
    private var clusterSet: MutableSet<String> = HashSet()

    override fun addCluster(cluster: String) {
        clusterSet.add(cluster)
    }

    override fun get(cluster: String): Register {
        if (allRegisters.containsKey(cluster)) {
            return allRegisters[cluster]!!
        }
        val register = create(cluster)
        allRegisters[cluster] = register
        return register
    }

    override fun remove(cluster: String): Register {
        return allRegisters.remove(cluster)!!
    }

    abstract fun create(cluster: String): Register

    override fun getClusterSet(): MutableSet<String> {
        return clusterSet
    }
}
