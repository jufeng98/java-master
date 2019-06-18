package org.javamaster.b2c.test.model.jackson;

import java.io.Serializable;
/**
 * @author yudong
 * @date 2019/6/18
 */
public class MemberInformation extends LoginUserInformation implements Serializable {

    private static final long serialVersionUID = 3334937746962893477L;
    private String fpcardno = "513712340023";
    private String passwd = "12346";

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getFpcardno() {
        return fpcardno;
    }

    public void setFpcardno(String fpcardno) {
        this.fpcardno = fpcardno;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}
