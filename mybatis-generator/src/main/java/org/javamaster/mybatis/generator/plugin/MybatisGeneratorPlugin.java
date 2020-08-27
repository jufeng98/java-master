package org.javamaster.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 * @date 2019/9/1
 */
public class MybatisGeneratorPlugin extends PluginAdapter {

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 添加entity类注释
        String docLine = "/**\n" +
                " * %s,请勿手工改动此文件,请使用 mybatis generator\n" +
                " * \n" +
                " * @author mybatis generator\n" +
                " */";
        docLine = String.format(docLine, introspectedTable.getRemarks());
        topLevelClass.addJavaDocLine(docLine);

        topLevelClass.addImportedType("org.apache.commons.lang3.builder.EqualsBuilder");
        topLevelClass.addImportedType("org.apache.commons.lang3.builder.HashCodeBuilder");
        topLevelClass.addImportedType("org.apache.commons.lang3.builder.ToStringBuilder");
        topLevelClass.addImportedType("org.apache.commons.lang3.builder.ToStringStyle");

        List<Method> newMethods = new ArrayList<>();
        Method toStringMethod = new Method();
        toStringMethod.addAnnotation("@Override");
        toStringMethod.setVisibility(JavaVisibility.PUBLIC);
        toStringMethod.setReturnType(FullyQualifiedJavaType.getStringInstance());
        toStringMethod.setName("toString");
        toStringMethod.addBodyLine("return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);");
        newMethods.add(toStringMethod);

        Method equalsMethod = new Method();
        equalsMethod.addAnnotation("@Override");
        equalsMethod.setVisibility(JavaVisibility.PUBLIC);
        equalsMethod.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        equalsMethod.setName("equals");
        equalsMethod.addParameter(0, new Parameter(FullyQualifiedJavaType.getObjectInstance(), "obj"));
        equalsMethod.addBodyLine("return EqualsBuilder.reflectionEquals(this, obj);");
        newMethods.add(equalsMethod);

        Method hashMethod = new Method();
        hashMethod.addAnnotation("@Override");
        hashMethod.setVisibility(JavaVisibility.PUBLIC);
        hashMethod.setReturnType(FullyQualifiedJavaType.getIntInstance());
        hashMethod.setName("hashCode");
        hashMethod.addBodyLine("return HashCodeBuilder.reflectionHashCode(this);");
        newMethods.add(hashMethod);

        newMethods.addAll(topLevelClass.getMethods());

        try {
            java.lang.reflect.Field field = ReflectionUtils.findField(topLevelClass.getClass(), "methods");
            field.setAccessible(true);
            field.set(topLevelClass, newMethods);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn
            , IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        // 添加entity字段注释
        String docLine = "/**\n" +
                "     * %s\n" +
                "     */";
        field.addJavaDocLine(String.format(docLine, introspectedColumn.getRemarks()));
        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        // 添加entity get方法注释
        String docLine = "/**\n" +
                "     * 获取%s\n" +
                "     */";
        method.addJavaDocLine(String.format(docLine, introspectedColumn.getRemarks()));
        return super.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        // 添加entity set方法注释
        String docLine = "/**\n" +
                "     * 设置%s\n" +
                "     */";
        method.addJavaDocLine(String.format(docLine, introspectedColumn.getRemarks()));
        return super.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 添加example类注释
        String docLine = "/**\n" +
                " * 请勿手工改动此文件,请使用 mybatis generator\n" +
                " * \n" +
                " * @author mybatis generator\n" +
                " */";
        topLevelClass.addJavaDocLine(docLine);
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
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
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addGenerateKey(element, introspectedTable);
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return addGenerateKey(element, introspectedTable);
    }

    private boolean addGenerateKey(XmlElement element, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = introspectedTable.getPrimaryKeyColumns();
        if (columns.size() == 1) {
            element.addAttribute(new Attribute("useGeneratedKeys", "true"));
            element.addAttribute(new Attribute("keyProperty", columns.get(0).getJavaProperty()));
        }
        return super.sqlMapInsertElementGenerated(element, introspectedTable);
    }

}
