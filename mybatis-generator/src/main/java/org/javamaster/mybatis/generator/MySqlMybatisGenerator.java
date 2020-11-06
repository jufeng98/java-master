package org.javamaster.mybatis.generator;

import org.javamaster.mybatis.generator.utils.PropertiesUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StreamUtils;
import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    private static final Logger logger = LoggerFactory.getLogger(MySqlMybatisGenerator.class);

    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    private static final XPathFactory X_PATH_FACTORY = XPathFactory.newInstance();

    static {
        try {
            FACTORY.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        try {
            generator();
        } catch (Exception e) {
            logger.error("generated error", e);
        }
    }

    public static void generator() throws Exception {
        InputStream inputStream = null;
        try {
            Properties properties = PropertiesUtils.getInstance();
            inputStream = addTableToXml();
            inputStream.mark(inputStream.available());
            logger.info("xml content:\r\n{}", StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
            inputStream.reset();
            List<String> warnings = new ArrayList<>();
            ConfigurationParser cp = new ConfigurationParser(properties, warnings);
            Configuration config = cp.parseConfiguration(inputStream);
            DefaultShellCallback callback = new DefaultShellCallback(true);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);
            logger.info("generated warnings:");
            warnings.forEach(System.out::println);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private static InputStream addTableToXml() throws Exception {
        InputStream xmlInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            URL url = MySqlMybatisGenerator.class.getClassLoader().getResource("generatorConfig.xml");
            assert url != null;
            UrlResource urlResource = new UrlResource(url);
            DocumentBuilder documentBuilder = FACTORY.newDocumentBuilder();
            xmlInputStream = urlResource.getInputStream();
            Document doc = documentBuilder.parse(xmlInputStream);
            Element rootEle = doc.getDocumentElement();
            XPath xPath = X_PATH_FACTORY.newXPath();

            Node parentNode = (Node) xPath.evaluate("/generatorConfiguration/context", rootEle, XPathConstants.NODE);

            NodeList nodeList = (NodeList) xPath.evaluate("/generatorConfiguration/context/table", rootEle, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                parentNode.removeChild(nodeList.item(i));
            }

            boolean disableComment = PropertiesUtils.getPropAsBoolean("disable.comment", "false");
            boolean tkMybatis = PropertiesUtils.getPropAsBoolean("is.tk.mybatis", "false");
            if (tkMybatis || disableComment) {
                Node node = (Node) xPath
                        .evaluate("/generatorConfiguration/context/plugin[@type='org.javamaster.mybatis.generator.plugin.MybatisGeneratorPlugin']", rootEle, XPathConstants.NODE);
                parentNode.removeChild(node);
            }
            if (!tkMybatis) {
                Node node = (Node) xPath
                        .evaluate("/generatorConfiguration/context/plugin[@type='org.javamaster.mybatis.generator.plugin.TkMapperPlugin']", rootEle, XPathConstants.NODE);
                parentNode.removeChild(node);
            } else {
                Node node = parentNode.getAttributes().getNamedItem("targetRuntime");
                node.setNodeValue("MyBatis3Simple");
            }

            String jdbcUrl = PropertiesUtils.getProp("jdbc.url");
            URI uri = new URI(jdbcUrl.replace("jdbc:mysql", "http"));
            String database = uri.getPath().replace("/", "");
            String[] tables = PropertiesUtils.getProp("tables").split(",");
            for (String tableName : tables) {
                Element element = doc.createElement("table");
                element.setAttribute("tableName", tableName);
                element.setAttribute("schema", database);
                parentNode.appendChild(element);
            }

            handlerAutoIncrement(xPath, doc);

            changeDatabaseTinyintToInteger(xPath, doc);

            DOMImplementation domImplementation = doc.getImplementation();
            DOMImplementationLS domImplementationLs = (DOMImplementationLS) domImplementation.getFeature("LS", "3.0");
            LSOutput lsOutput = domImplementationLs.createLSOutput();
            lsOutput.setEncoding("UTF-8");
            byteArrayOutputStream = new ByteArrayOutputStream();
            lsOutput.setByteStream(byteArrayOutputStream);
            LSSerializer lsSerializer = domImplementationLs.createLSSerializer();
            lsSerializer.write(doc, lsOutput);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } finally {
            if (xmlInputStream != null) {
                xmlInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
        }
    }

    /**
     * 修改所有db列类型为TINYINT的,生成到Java里用Integer表示,通过修改generatorConfig.xml的table元素来完成这个任务
     */
    private static void changeDatabaseTinyintToInteger(XPath xPath, Document doc) throws Exception {
        Element rootEle = doc.getDocumentElement();
        boolean close = PropertiesUtils.getPropAsBoolean("disable.tinyint.to.integer", "true");
        if (close) {
            return;
        }
        try (Connection connection = DriverManager.getConnection(PropertiesUtils.getProp("jdbc.url"),
                PropertiesUtils.getProp("jdbc.user"), PropertiesUtils.getProp("jdbc.password"))) {
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

    /**
     * 处理自动递增主键
     */
    private static void handlerAutoIncrement(XPath xPath, Document doc) throws Exception {
        Element rootEle = doc.getDocumentElement();
        try (Connection connection = DriverManager.getConnection(PropertiesUtils.getProp("jdbc.url"),
                PropertiesUtils.getProp("jdbc.user"), PropertiesUtils.getProp("jdbc.password"))) {
            NodeList tableNodeList = (NodeList) xPath.evaluate("//table", rootEle, XPathConstants.NODESET);
            for (int i = 0; i < tableNodeList.getLength(); i++) {
                Element tableEle = ((Element) tableNodeList.item(i));
                String tableName = tableEle.getAttribute("tableName");
                DatabaseMetaData databaseMetaData = connection.getMetaData();

                boolean supportsIsAutoIncrement = false;
                ResultSet resultSet = databaseMetaData.getColumns(null, null, tableName, "%");
                ResultSetMetaData metaData = resultSet.getMetaData();
                for (int j = 1; j <= metaData.getColumnCount(); j++) {
                    if ("IS_AUTOINCREMENT".equals(metaData.getColumnName(j))) {
                        supportsIsAutoIncrement = true;
                        break;
                    }
                }
                if (!supportsIsAutoIncrement) {
                    return;
                }

                ResultSet tableResultSet = databaseMetaData.getPrimaryKeys(null, null, tableName);
                while (tableResultSet.next()) {
                    String columnName = tableResultSet.getString("COLUMN_NAME");
                    Element generatedKeyEle = doc.createElement("generatedKey");
                    generatedKeyEle.setAttribute("column", columnName);
                    generatedKeyEle.setAttribute("sqlStatement", "MySql");
                    generatedKeyEle.setAttribute("identity", "true");
                    tableEle.appendChild(generatedKeyEle);
                }
            }
        }
    }

}
