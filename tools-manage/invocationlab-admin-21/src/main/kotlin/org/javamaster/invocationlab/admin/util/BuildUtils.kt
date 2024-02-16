package org.javamaster.invocationlab.admin.util

import org.javamaster.invocationlab.admin.consts.Constant

/**
 * @author yudong
 */
object BuildUtils {
    private const val SPLITTER = "_"

    @JvmStatic
    fun buildServiceKey(cluster: String, serviceName: String): String {
        return cluster + SPLITTER + serviceName
    }

    @JvmStatic
    fun buildInterfaceKey(group: String, interfaceName: String, version: String?): String {
        var versionAppend = version
        if (versionAppend.isNullOrEmpty()) {
            versionAppend = Constant.DEFAULT_VERSION
        }
        return group + SPLITTER + interfaceName + SPLITTER + versionAppend
    }

    @JvmStatic
    fun getGroupByInterfaceKey(interfaceKey: String): String {
        return interfaceKey.split(SPLITTER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    }

    @JvmStatic
    fun getInterfaceNameByInterfaceKey(interfaceKey: String): String {
        return interfaceKey.split(SPLITTER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
    }

    @JvmStatic
    fun getVersionByInterfaceKey(interfaceKey: String): String {
        return interfaceKey.split(SPLITTER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2]
    }

    @JvmStatic
    fun getJavaMethodName(methodName: String): String {
        return methodName.split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    }

    @JvmStatic
    fun buildMethodNameKey(
        cluster: String,
        serviceName: String,
        group: String,
        interfaceName: String,
        version: String?,
        methodName: String
    ): String {
        val serviceKey = buildServiceKey(cluster, serviceName)
        val interfaceKey = buildInterfaceKey(group, interfaceName, version)
        return serviceKey + SPLITTER + interfaceKey + SPLITTER + methodName
    }

    @JvmStatic
    fun getMethodNameKey(
        cluster: String,
        serviceName: String,
        interfaceKey: String,
        methodName: String
    ): String {
        val serviceKey = buildServiceKey(cluster, serviceName)
        return serviceKey + SPLITTER + interfaceKey + SPLITTER + methodName
    }

    /**
     * 把ip地址拼接成zk地址
     * zookeeper://127.0.0.1:2181?backup=127.0.0.2:2181,127.0.0.3:2181
     */
    @JvmStatic
    fun buildZkUrl(zk: String): String {
        val zkRegis: StringBuilder
        val prefix = "zookeeper://"
        if (zk.contains(",")) {
            val zs = zk.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            zkRegis = StringBuilder(prefix + zs[0])
            if (zs.size > 1) {
                zkRegis.append("?backup=")
                for (index in 1 until zs.size) {
                    zkRegis.append(zs[index])
                    if (index < zs.size - 1) {
                        zkRegis.append(",")
                    }
                }
            }
        } else {
            zkRegis = StringBuilder(prefix + zk)
        }
        return zkRegis.toString()
    }
}
