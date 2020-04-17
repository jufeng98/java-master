package org.javamaster.mybatis.generator.plugin;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.javamaster.mybatis.generator.EnumMustacheBean;
import org.javamaster.mybatis.generator.EnumMustacheField;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 根据注释生成枚举类,注释格式必须满足如下所示:<br/>
 * 考试类型,枚举-PUBLIC:1:公开考试;SPECIFIC:2:特殊考试
 *
 * @author yudong
 * @date 2020/1/10
 */
public class MybatisEnumsPlugin extends PluginAdapter {

    private static final String ENUM_FLAG = "枚举";
    private MustacheFactory mf = new DefaultMustacheFactory();

    private String enumPath;
    private String targetProject;

    @Override
    public void setContext(Context context) {
        try {
            File propFile = ResourceUtils.getFile("classpath:generatorConfig.properties");
            Properties properties = new Properties();
            properties.load(new FileInputStream(propFile));
            enumPath = properties.getProperty("enums.package");
            targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn
            , IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        String remark = introspectedColumn.getRemarks();
        if (!remark.contains(ENUM_FLAG)) {
            return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
        }

        String enumClassName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1) + "Enum";
        String enumValue = remark.split("-")[1];
        String[] enums = enumValue.split(";");

        List<EnumMustacheField> list = Arrays.stream(enums)
                .map(s -> {
                    String[] tmp = s.split(":");
                    EnumMustacheField enumMustacheField = new EnumMustacheField();
                    enumMustacheField.setLabel(tmp[0]);
                    enumMustacheField.setCode(Integer.parseInt(tmp[1]));
                    enumMustacheField.setMsg(tmp[2]);
                    return enumMustacheField;
                })
                .collect(Collectors.toList());


        EnumMustacheBean enumMustacheBean = new EnumMustacheBean();
        enumMustacheBean.setEnumPath(enumPath);
        enumMustacheBean.setEnumClassName(enumClassName);
        enumMustacheBean.setEnumFields(list);

        DefaultShellCallback callback = new DefaultShellCallback(true);
        try {
            File directory = callback.getDirectory(targetProject, enumPath);
            File targetFile = new File(directory, enumClassName + ".java");
            Mustache mustache = mf.compile("template/Enum.mustache");
            mustache.execute(new PrintWriter(targetFile), enumMustacheBean).flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }


}
