package org.javamaster.b2c.core.entity;

import java.io.Serializable;

/**
 * 系统用户表
 * 
 * @author mybatis generator
 * @date 2019/06/13 09:37:47
 */
public class SysUser implements Serializable {
    /**
     * 用户名
     */
    private String username;

    /**
     * 加密后密码
     */
    private String password;

    /**
     * 用户状态,0不可用;1:可用
     */
    private Byte enabled;

    private static final long serialVersionUID = 1821414069229130752L;

    /**
     * 获取用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取加密后密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置加密后密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取用户状态,0不可用;1:可用
     */
    public Byte getEnabled() {
        return enabled;
    }

    /**
     * 设置用户状态,0不可用;1:可用
     */
    public void setEnabled(Byte enabled) {
        this.enabled = enabled;
    }
}