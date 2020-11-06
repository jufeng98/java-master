package org.javamaster.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import tk.mybatis.mapper.generator.MapperPlugin;

import static org.javamaster.mybatis.generator.plugin.MybatisGeneratorPlugin.appendComment;
import static org.javamaster.mybatis.generator.plugin.MybatisGeneratorPlugin.appendMethod;

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
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        appendComment(document);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

}
