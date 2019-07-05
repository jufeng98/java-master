package org.javamaster.b2c.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;

/**
 * Created on 2018/12/12.<br/>
 *
 * @author yudong
 */
public class Page implements Serializable {

    private static final long serialVersionUID = 2775511245499344608L;
    /**
     * 页码，从1开始
     */
    @Range(min = 1)
    private Integer pageNum;
    /**
     * 页面大小
     */
    @Range(min = 1)
    private Integer pageSize;
    /**
     * 排序
     */
    private String orderBy;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(ToStringStyle.JSON_STYLE);
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
