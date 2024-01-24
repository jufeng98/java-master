package org.javamaster.invocationlab.admin.model.sso;

import lombok.Data;

@Data
public class GetUserInfoResVo {
    private String email;
    private String account;
    private String realName;
    private String blood;
    private String mobileNo;
    private String seqnr;
    private Integer accountType;
    private String sex;
    private String empType;
}
