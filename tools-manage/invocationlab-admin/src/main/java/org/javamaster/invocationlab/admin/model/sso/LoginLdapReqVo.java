package org.javamaster.invocationlab.admin.model.sso;

import lombok.Data;

@Data
public class LoginLdapReqVo {
    private String account;
    private Integer pwdSwitch;
    private String appType;
    private String clientType;
    private String password;
    private String deviceNum;
    private String manuFacturer;
    private Integer accountType;
}
