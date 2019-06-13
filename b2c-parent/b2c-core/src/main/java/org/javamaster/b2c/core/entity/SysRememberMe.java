package org.javamaster.b2c.core.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统记住登录用户表
 * 
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public class SysRememberMe implements Serializable {
    /**
     * 序列号
     */
    private String series;

    /**
     * 关联用户表用户名
     */
    private String username;

    /**
     * token
     */
    private String token;

    /**
     * 最后使用时间
     */
    private Date lastUsed;

    private static final long serialVersionUID = 674742322090118041L;

    /**
     * 获取序列号
     */
    public String getSeries() {
        return series;
    }

    /**
     * 设置序列号
     */
    public void setSeries(String series) {
        this.series = series;
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

    /**
     * 获取token
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 获取最后使用时间
     */
    public Date getLastUsed() {
        return lastUsed;
    }

    /**
     * 设置最后使用时间
     */
    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }
}