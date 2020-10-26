package org.javamaster.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import tk.mybatis.mapper.generator.MapperPlugin;

/**
 * @author yudong
 * @date 2020/10/26
 */
public class TkMapperPlugin extends MapperPlugin {

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
        // 添加entity类注释
        String docLine = "/**\n" +
                " * %s,请勿手工改动此文件,请使用 mybatis generator\n" +
                " * \n" +
                " * @author mybatis generator\n" +
                " */";
        docLine = String.format(docLine, introspectedTable.getRemarks());
        topLevelClass.addJavaDocLine(docLine);
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        super.sqlMapDocumentGenerated(document, introspectedTable);
        Element element = new Element() {
            @Override
            public String getFormattedContent(int indentLevel) {
                StringBuilder sb = new StringBuilder();
                OutputUtilities.xmlIndent(sb, indentLevel);
                sb.append("<!-- 此文件由 mybatis generator 生成,注意: 请勿手工改动此文件, 请使用 mybatis generator -->");
                return sb.toString();
            }
        };
        document.getRootElement().addElement(0, element);
        return true;
    }

}
