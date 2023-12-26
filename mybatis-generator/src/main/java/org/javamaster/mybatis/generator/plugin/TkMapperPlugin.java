package org.javamaster.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import tk.mybatis.mapper.generator.MapperPlugin;

import static org.javamaster.mybatis.generator.plugin.MybatisGeneratorPlugin.appendComment;
import static org.javamaster.mybatis.generator.plugin.MybatisGeneratorPlugin.appendMethod;
import static org.javamaster.mybatis.generator.plugin.MybatisGeneratorPlugin.createConstField;

/**
 * @author yudong
 * @date 2020/10/26
 */
public class TkMapperPlugin extends MapperPlugin {

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        appendMethod(topLevelClass, introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 添加mapper类注释
        String docLine = "/**\n" +
                " * 操纵%s,请勿手工改动此文件,请使用 mybatis generator\n" +
                " * \n" +
                " * @author mybatis generator\n" +
                " */";
        docLine = String.format(docLine, introspectedTable.getRemarks());
        interfaze.addJavaDocLine(docLine);
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        appendComment(document);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn
            , IntrospectedTable introspectedTable, ModelClassType modelClassType) {

        topLevelClass.addField(createConstField(field));

        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

}
