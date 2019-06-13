package org.javamaster.b2c.core.entity;

import java.io.Serializable;

/**
 * 系统组表
 * 
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public class SysGroup implements Serializable {
    /**
     * 组编码
     */
    private String groupCode;

    /**
     * 组名称
     */
    private String groupName;

    private static final long serialVersionUID = 185646671827248761L;

    /**
     * 获取组编码
     */
    public String getGroupCode() {
        return groupCode;
    }

    /**
     * 设置组编码
     */
    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    /**
     * 获取组名称
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * 设置组名称
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}