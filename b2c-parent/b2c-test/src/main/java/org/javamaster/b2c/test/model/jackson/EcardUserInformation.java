package org.javamaster.b2c.test.model.jackson;

import java.io.Serializable;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class EcardUserInformation extends LoginUserInformation implements Serializable {

    private static final long serialVersionUID = -8191715041208933113L;
    private String aid;
    private String cellPhone;
    private String email;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
