package org.javamaster.invocationlab.admin.model.sso;

import lombok.Data;

@Data
public class GetUserInfoReqVo {
    private String account;
    private String appType;
    private String accountType;
}
