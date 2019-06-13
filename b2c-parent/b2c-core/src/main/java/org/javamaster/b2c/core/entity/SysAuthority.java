package org.javamaster.b2c.core.entity;

import java.io.Serializable;

/**
 * 系统权限表
 * 
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public class SysAuthority implements Serializable {
    /**
     * 权限编码
     */
    private String authorityCode;

    /**
     * 权限名称
     */
    private String authorityName;

    private static final long serialVersionUID = 895737410191303026L;

    /**
     * 获取权限编码
     */
    public String getAuthorityCode() {
        return authorityCode;
    }

    /**
     * 设置权限编码
     */
    public void setAuthorityCode(String authorityCode) {
        this.authorityCode = authorityCode;
    }

    /**
     * 获取权限名称
     */
    public String getAuthorityName() {
        return authorityName;
    }

    /**
     * 设置权限名称
     */
    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }
}