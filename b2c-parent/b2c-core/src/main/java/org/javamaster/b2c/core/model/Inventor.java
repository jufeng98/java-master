package org.javamaster.b2c.core.model;


import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * @author yudong
 * @date 2020/6/5
 */
public class Inventor {
    private String name;
    private Date time;
    private String serbian;

    public Inventor(String name, String serbian) {
        this.name = name;
        this.serbian = serbian;
    }

    public Inventor(String name, Date time, String serbian) {
        this.name = name;
        this.time = time;
        this.serbian = serbian;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("time", time)
                .append("serbian", serbian)
                .toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getSerbian() {
        return serbian;
    }

    public void setSerbian(String serbian) {
        this.serbian = serbian;
    }
}
