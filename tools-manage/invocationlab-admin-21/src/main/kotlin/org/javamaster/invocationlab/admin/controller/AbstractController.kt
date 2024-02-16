package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.service.context.InvokeContext.getService
import org.javamaster.invocationlab.admin.util.BuildUtils.buildServiceKey
import org.apache.commons.lang3.tuple.Pair
import org.springframework.stereotype.Service

/**
 * 提供一些公共的模板方法
 *
 * @author yudong
 */
@Service
abstract class AbstractController {

    fun getAllSimpleClassName(zk: String, serviceName: String): Map<String, String> {
        val modelKey = buildServiceKey(zk, serviceName)
        val interfaceMap: MutableMap<String, String> = LinkedHashMap(10)
        val postmanService = getService(modelKey)

        postmanService!!.getInterfaceModels().stream()
            .map { interfaceEntity ->
                val className: String = interfaceEntity.interfaceName!!
                val simpleClassName = className.substring(className.lastIndexOf(".") + 1)
                Pair.of(simpleClassName, interfaceEntity.key)
            }
            .sorted(Comparator.comparing { obj -> obj.left })
            .forEach { pair -> interfaceMap[pair.left] = pair.right!! }
        return interfaceMap
    }

}
