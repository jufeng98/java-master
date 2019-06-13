package org.javamaster.b2c.core.entity;

import java.io.Serializable;

/**
 * 系统组表关联用户表
 * 
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public class SysGroupUser implements Serializable {
    /**
     * 关联组表组编码
     */
    private String groupCode;

    /**
     * 关联用户表用户名
     */
    private String username;

    private static final long serialVersionUID = 239003777212091765L;

    /**
     * 获取关联组表组编码
     */
    public String getGroupCode() {
        return groupCode;
    }

    /**
     * 设置关联组表组编码
     */
    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
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