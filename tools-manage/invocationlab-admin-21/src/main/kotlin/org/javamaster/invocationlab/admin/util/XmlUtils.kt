package org.javamaster.invocationlab.admin.util

import org.javamaster.invocationlab.admin.config.BizException
import org.javamaster.invocationlab.admin.service.GAV
import com.google.common.collect.Maps
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author yudong
 */
object XmlUtils {

    fun parseDependencyXml(dependency: String): Map<String, String> {
        val dependencyMap: MutableMap<String, String> = Maps.newHashMap()
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()

        val bytes = dependency.toByteArray()
        val bi = ByteArrayInputStream(bytes)
        val doc = dBuilder.parse(bi)
        doc.documentElement.normalize()
        var nList = doc.getElementsByTagName("groupId")
        var content = nList.item(0).textContent
        dependencyMap["groupId"] = content.trim { it <= ' ' }
        nList = doc.getElementsByTagName("artifactId")
        content = nList.item(0).textContent
        dependencyMap["artifactId"] = content.trim { it <= ' ' }
        nList = doc.getElementsByTagName("version")
        if (nList.length != 0) {
            val item = nList.item(0)
            content = item.textContent
        } else {
            content = ""
        }
        dependencyMap["version"] = content.trim { it <= ' ' }
        return dependencyMap
    }

    @JvmStatic
    fun parseGav(dependency: String): GAV {
        val dm = parseDependencyXml(dependency)
        if (dm.size < 2) {
            throw BizException("dependency格式不对,请指定正确的maven dependency,区分大小写")
        }
        val g = dm["groupId"]
        val a = dm["artifactId"]
        val v = dm["version"]

        val gav = GAV()
        gav.groupID = g
        gav.artifactID = a
        gav.version = v
        return gav
    }
}
