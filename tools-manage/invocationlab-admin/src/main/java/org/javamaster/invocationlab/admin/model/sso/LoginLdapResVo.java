package org.javamaster.invocationlab.admin.model.sso;

import lombok.Data;

@Data
public class LoginLdapResVo {
    private String refreshToken;
    private String email;
    private String account;
    private String realName;
    private String token;
    private String blood;
    private String mobileNo;
    private Integer accountType;
    private String sex;
    private String empType;
}
