package org.javamaster.mybatis.generator.model;

import java.util.List;

/**
 * @author yudong
 * @date 2020/1/15
 */
public class EnumMustacheBean {
    private String enumPath;
    private String enumClassName;
    private List<EnumMustacheField> enumFields;

    public String getEnumPath() {
        return enumPath;
    }

    public void setEnumPath(String enumPath) {
        this.enumPath = enumPath;
    }

    public String getEnumClassName() {
        return enumClassName;
    }

    public void setEnumClassName(String enumClassName) {
        this.enumClassName = enumClassName;
    }

    public List<EnumMustacheField> getEnumFields() {
        return enumFields;
    }

    public void setEnumFields(List<EnumMustacheField> enumFields) {
        this.enumFields = enumFields;
    }
}
