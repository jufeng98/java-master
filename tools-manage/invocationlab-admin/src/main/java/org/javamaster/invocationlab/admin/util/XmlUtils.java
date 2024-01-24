package org.javamaster.invocationlab.admin.util;

import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.service.GAV;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Map;

/**
 * @author yudong
 */
public class XmlUtils {

    @SneakyThrows
    public static Map<String, String> parseDependencyXml(String dependency) {
        Map<String, String> dependencyMap = Maps.newHashMap();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        byte[] bytes = dependency.getBytes();
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        Document doc = dBuilder.parse(bi);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("groupId");
        String content = nList.item(0).getTextContent();
        dependencyMap.put("groupId", content.trim());
        nList = doc.getElementsByTagName("artifactId");
        content = nList.item(0).getTextContent();
        dependencyMap.put("artifactId", content.trim());
        nList = doc.getElementsByTagName("version");
        if (nList.getLength() != 0) {
            Node item = nList.item(0);
            content = item.getTextContent();
        } else {
            content = "";
        }
        dependencyMap.put("version", content.trim());
        return dependencyMap;
    }

    public static GAV parseGav(String dependency) {
        Map<String, String> dm = XmlUtils.parseDependencyXml(dependency);
        if (dm.size() < 2) {
            throw new BizException("dependency格式不对,请指定正确的maven dependency,区分大小写");
        }
        String g = dm.get("groupId");
        String a = dm.get("artifactId");
        String v = dm.get("version");

        GAV gav = new GAV();
        gav.setGroupID(g);
        gav.setArtifactID(a);
        gav.setVersion(v);
        return gav;
    }

}
