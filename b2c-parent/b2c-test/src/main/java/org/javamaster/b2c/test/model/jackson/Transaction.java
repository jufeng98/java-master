package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class Transaction {
    /**
     * 单独指定序列化后的格式和反序列化时以此格式来解析日期字符串
     */
    @JsonFormat(pattern = "yyyyMMdd HH:mm:ssSSS", locale = "zh_CN", timezone = "GMT+8")
    private Date date = null;
    private Date create = null;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getCreate() {
        return create;
    }

    public void setCreate(Date create) {
        this.create = create;
    }
}
