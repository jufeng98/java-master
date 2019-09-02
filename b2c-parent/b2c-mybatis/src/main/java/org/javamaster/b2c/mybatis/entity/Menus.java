package org.javamaster.b2c.mybatis.entity;

import java.util.Date;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 菜单表,请勿手工改动此文件,请使用 mybatis generator
 * 
 * @author mybatis generator
 */
public class Menus {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 父id,0:顶级菜单;1:非顶级菜单
     */
    private Integer parentId;

    /**
     * 是否有子菜单,0:无;1:有
     */
    private Byte hasSubMenu;

    /**
     * 名称
     */
    private String name;

    /**
     * path
     */
    private String path;

    /**
     * 是否应显示,0:否;1:是
     */
    private Byte shouldShow;

    /**
     * 图标类名
     */
    private String icon;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后操作时间
     */
    private Date opTime;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * 获取主键id
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置主键id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取父id,0:顶级菜单;1:非顶级菜单
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * 设置父id,0:顶级菜单;1:非顶级菜单
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取是否有子菜单,0:无;1:有
     */
    public Byte getHasSubMenu() {
        return hasSubMenu;
    }

    /**
     * 设置是否有子菜单,0:无;1:有
     */
    public void setHasSubMenu(Byte hasSubMenu) {
        this.hasSubMenu = hasSubMenu;
    }

    /**
     * 获取名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取path
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取是否应显示,0:否;1:是
     */
    public Byte getShouldShow() {
        return shouldShow;
    }

    /**
     * 设置是否应显示,0:否;1:是
     */
    public void setShouldShow(Byte shouldShow) {
        this.shouldShow = shouldShow;
    }

    /**
     * 获取图标类名
     */
    public String getIcon() {
        return icon;
    }

    /**
     * 设置图标类名
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 获取创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取最后操作时间
     */
    public Date getOpTime() {
        return opTime;
    }

    /**
     * 设置最后操作时间
     */
    public void setOpTime(Date opTime) {
        this.opTime = opTime;
    }
}