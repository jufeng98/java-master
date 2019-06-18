package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * @author yudong
 * @date 2019/6/18
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MemberInformation.class),
        @JsonSubTypes.Type(value = EcardUserInformation.class)})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class LoginUserInformation implements Serializable {

    private static final long serialVersionUID = -933578885036345619L;

    private String userType = "1";
    private String ip = "127.0.0.1";

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
