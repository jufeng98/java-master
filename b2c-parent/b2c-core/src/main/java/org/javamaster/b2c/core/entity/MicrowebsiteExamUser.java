package org.javamaster.b2c.core.entity;

import java.io.Serializable;

/**
 * 考试关联用户表
 * 
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public class MicrowebsiteExamUser implements Serializable {
    /**
     * 关联考试表考试编码
     */
    private String examCode;

    /**
     * 关联用户表用户名
     */
    private String username;

    private static final long serialVersionUID = 953803833692898660L;

    /**
     * 获取关联考试表考试编码
     */
    public String getExamCode() {
        return examCode;
    }

    /**
     * 设置关联考试表考试编码
     */
    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    /**
     * 获取关联用户表用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置关联用户表用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }
}