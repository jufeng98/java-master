package org.javamaster.invocationlab.admin.service.registry.impl

import org.javamaster.invocationlab.admin.service.registry.Register
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import org.springframework.web.client.RestTemplate
import java.util.concurrent.ConcurrentHashMap

/**
 * @author yudong
 */
class EurekaRegister(private val cluster: String, private val restTemplate: RestTemplate?) : Register {
    override fun pullData() {
        throw UnsupportedOperationException()
    }

    override fun getAllService(): Map<String, Map<String, InterfaceMetaInfo>> {
        val map: MutableMap<String, Map<String, InterfaceMetaInfo>> = Maps.newHashMap()
        val jsonNode = restTemplate!!.getForObject("http://$cluster/eureka/apps/", JsonNode::class.java)
        val arrayNode = jsonNode!!["applications"]["application"] as ArrayNode
        for (node in arrayNode) {
            map[node["name"].asText()] = emptyMap()
        }
        return map
    }

    override fun getServiceInstances(serviceName: String): List<String> {
        var instances = INSTANCES_CACHE[serviceName]
        if (instances != null) {
            return instances
        }
        instances = Lists.newArrayList()
        val jsonNode = restTemplate!!.getForObject("http://$cluster/eureka/apps/$serviceName", JsonNode::class.java)
        val arrayNode = jsonNode!!["application"]["instance"] as ArrayNode
        for (node in arrayNode) {
            instances.add(node["ipAddr"].asText() + ":" + node["port"]["$"].asText())
        }
        INSTANCES_CACHE[serviceName] = instances
        return instances
    }

    companion object {
        private val INSTANCES_CACHE: MutableMap<String, MutableList<String>> = ConcurrentHashMap()
        fun clearCache() {
            INSTANCES_CACHE.clear()
        }
    }
}
