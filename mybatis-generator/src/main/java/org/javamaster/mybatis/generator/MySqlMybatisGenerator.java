package org.javamaster.mybatis.generator;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 生成mybatis model,mapper等文件
 *
 * @author yudong
 * @date 2019/9/1
 */
public class MySqlMybatisGenerator {

    private static Logger logger = LoggerFactory.getLogger(MySqlMybatisGenerator.class);

    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private static XPathFactory xPathFactory = XPathFactory.newInstance();


    public static void main(String[] args) {
        try {
            generator();
        } catch (Exception e) {
            logger.error("generated error", e);
        }
    }

    public static void generator() throws Exception {
        List<String> warnings = new ArrayList<>();
        File propFile = ResourceUtils.getFile("classpath:generatorConfig.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(propFile));

        InputStream inputStream = addTableToXml(properties.getProperty("tables"));

        ConfigurationParser cp = new ConfigurationParser(properties, warnings);
        Configuration config = cp.parseConfiguration(inputStream);
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        inputStream.close();
        logger.info("generated warnings:" + warnings);
    }

    private static InputStream addTableToXml(String tablesStr) throws Exception {
        URL url = MySqlMybatisGenerator.class.getClassLoader().getResource("generatorConfig.xml");
        assert url != null;
        UrlResource urlResource = new UrlResource(url);

        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document doc = documentBuilder.parse(urlResource.getInputStream());
        Element rootEle = doc.getDocumentElement();
        XPath xPath = xPathFactory.newXPath();

        Node parentNode = (Node) xPath.evaluate("/generatorConfiguration/context", rootEle, XPathConstants.NODE);

        NodeList nodeList = (NodeList) xPath.evaluate("/generatorConfiguration/context/table", rootEle, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            parentNode.removeChild(nodeList.item(i));
        }

        String[] tables = tablesStr.split(",");
        for (String string : tables) {
            String[] tmp = string.split("\\.");
            Element element = doc.createElement("table");
            element.setAttribute("tableName", tmp[1]);
            element.setAttribute("schema", tmp[0]);
            parentNode.appendChild(element);
        }
        DOMImplementation domImplementation = doc.getImplementation();
        DOMImplementationLS domImplementationLS = (DOMImplementationLS) domImplementation.getFeature("LS", "3.0");
        LSOutput lsOutput = domImplementationLS.createLSOutput();
        lsOutput.setEncoding("UTF-8");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        lsOutput.setByteStream(byteArrayOutputStream);
        LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
        lsSerializer.write(doc, lsOutput);
        byteArrayOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

}
