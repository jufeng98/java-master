package org.javamaster.b2c.dubbo.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yudong
 * @date 2019/6/13
 */
public class UserBaseDto implements Serializable {
    private static final long serialVersionUID = -2691949559530045766L;

    private String username;
    private String mobile;
    private Date registerTime;

    @Override
    public String toString() {
        return "UserBaseDto{" +
                "username='" + username + '\'' +
                ", mobile='" + mobile + '\'' +
                ", registerTime=" + registerTime +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime) {
        this.registerTime = registerTime;
    }
}
