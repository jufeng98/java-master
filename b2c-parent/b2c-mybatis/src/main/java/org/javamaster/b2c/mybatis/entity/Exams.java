package org.javamaster.b2c.mybatis.entity;

import java.util.Date;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 考试表,请勿手工改动此文件,请使用 mybatis generator
 * 
 * @author mybatis generator
 */
public class Exams {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 考试编码,E+5位数字
     */
    private String examsCode;

    /**
     * 考试名称
     */
    private String examsName;

    /**
     * 考试说明
     */
    private String examsDesc;

    /**
     * 关联证书表certs_id
     */
    private Integer certsId;

    /**
     * 创建人名称
     */
    private String createUsername;

    /**
     * 创建时间
     */
    private Date createTime;

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
     * 获取考试编码,E+5位数字
     */
    public String getExamsCode() {
        return examsCode;
    }

    /**
     * 设置考试编码,E+5位数字
     */
    public void setExamsCode(String examsCode) {
        this.examsCode = examsCode;
    }

    /**
     * 获取考试名称
     */
    public String getExamsName() {
        return examsName;
    }

    /**
     * 设置考试名称
     */
    public void setExamsName(String examsName) {
        this.examsName = examsName;
    }

    /**
     * 获取考试说明
     */
    public String getExamsDesc() {
        return examsDesc;
    }

    /**
     * 设置考试说明
     */
    public void setExamsDesc(String examsDesc) {
        this.examsDesc = examsDesc;
    }

    /**
     * 获取关联证书表certs_id
     */
    public Integer getCertsId() {
        return certsId;
    }

    /**
     * 设置关联证书表certs_id
     */
    public void setCertsId(Integer certsId) {
        this.certsId = certsId;
    }

    /**
     * 获取创建人名称
     */
    public String getCreateUsername() {
        return createUsername;
    }

    /**
     * 设置创建人名称
     */
    public void setCreateUsername(String createUsername) {
        this.createUsername = createUsername;
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
}