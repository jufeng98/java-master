package org.javamaster.mybatis.generator;

/**
 * @author yudong
 * @date 2020/1/15
 */
public class EnumMustacheField {
    private String label;
    private int code;
    private String msg;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
