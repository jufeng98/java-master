package org.javamaster.mybatis.generator;

import org.apache.commons.lang3.StringUtils;
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
import java.sql.*;
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

        String projectPath = new File("").getAbsolutePath();
        properties.put("project.path", projectPath);


        InputStream inputStream = addTableToXml(properties.getProperty("tables"), properties);

        ConfigurationParser cp = new ConfigurationParser(properties, warnings);
        Configuration config = cp.parseConfiguration(inputStream);
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        inputStream.close();
        logger.info("generated warnings:" + warnings);
    }

    private static InputStream addTableToXml(String tablesStr, Properties properties) throws Exception {
        URL url = MySqlMybatisGenerator.class.getClassLoader().getResource("generatorConfig.xml");
        assert url != null;
        UrlResource urlResource = new UrlResource(url);

        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
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

        changeDatabaseTinyintToInteger(xPath, doc, properties);

        DOMImplementation domImplementation = doc.getImplementation();
        DOMImplementationLS domImplementationLs = (DOMImplementationLS) domImplementation.getFeature("LS", "3.0");
        LSOutput lsOutput = domImplementationLs.createLSOutput();
        lsOutput.setEncoding("UTF-8");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        lsOutput.setByteStream(byteArrayOutputStream);
        LSSerializer lsSerializer = domImplementationLs.createLSSerializer();
        lsSerializer.write(doc, lsOutput);
        byteArrayOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    /**
     * 修改所有db列类型为TINYINT的,生成到Java里用Integer表示,通过修改generatorConfig.xml的table元素来完成这个任务
     */
    private static void changeDatabaseTinyintToInteger(XPath xPath, Document doc, Properties properties) throws Exception {
        Element rootEle = doc.getDocumentElement();
        String s = properties.getProperty("disable.tinyint.to.integer");
        if (StringUtils.isBlank(s)) {
            s = "true";
        }
        boolean disable = Boolean.parseBoolean(s);
        if (disable) {
            return;
        }
        try (Connection connection = DriverManager.getConnection(properties.getProperty("jdbc.url"),
                properties.getProperty("jdbc.user"), properties.getProperty("jdbc.password"))) {
            NodeList nodeList = (NodeList) xPath.evaluate("//table", rootEle, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element tableEle = ((Element) nodeList.item(i));
                if (tableEle.getElementsByTagName("columnOverride").getLength() != 0) {
                    continue;
                }
                String tableName = tableEle.getAttribute("tableName");
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                ResultSet rs = databaseMetaData.getColumns(null, null, tableName, null);
                while (rs.next()) {
                    int dataType = rs.getInt("DATA_TYPE");
                    if (Types.TINYINT != dataType && Types.BIT != dataType) {
                        continue;
                    }
                    String columnName = rs.getString("COLUMN_NAME");
                    Element columnOverrideEle = doc.createElement("columnOverride");
                    columnOverrideEle.setAttribute("column", columnName);
                    columnOverrideEle.setAttribute("javaType", "Integer");
                    columnOverrideEle.setAttribute("jdbcType", "INTEGER");
                    tableEle.appendChild(columnOverrideEle);
                }
            }
        }
    }

}
