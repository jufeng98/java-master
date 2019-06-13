package org.javamaster.b2c.core.entity;

import java.io.Serializable;

/**
 * 系统组表关联权限表
 * 
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public class SysGroupAuthority implements Serializable {
    /**
     * 关联组表组编码
     */
    private String groupCode;

    /**
     * 关联权限表权限编码
     */
    private String authorityCode;

    private static final long serialVersionUID = 285718852749281550L;

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
     * 获取关联权限表权限编码
     */
    public String getAuthorityCode() {
        return authorityCode;
    }

    /**
     * 设置关联权限表权限编码
     */
    public void setAuthorityCode(String authorityCode) {
        this.authorityCode = authorityCode;
    }
}