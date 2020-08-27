package org.javamaster.mybatis.generator.plugin;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.config.Context;

import java.util.List;

/**
 * @author yudong
 * @date 2020/8/27
 */
public class MybatisGeneratorOverridePlugin extends PluginAdapter {

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        // 重复生成xml文件时不合并已有文件
        sqlMap.setMergeable(false);
        return super.sqlMapGenerated(sqlMap, introspectedTable);
    }

}
