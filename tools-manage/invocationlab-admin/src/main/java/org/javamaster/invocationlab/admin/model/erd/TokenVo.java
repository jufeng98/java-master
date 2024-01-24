package org.javamaster.invocationlab.admin.model.erd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class TokenVo implements Serializable {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("expires_in")
    private int expiresIn;
    private String scope;
    @JsonProperty("tenant_id")
    private String tenantId;
    private String license;
    @JsonProperty("dept_id")
    private String deptId;
    @JsonProperty("dept_name")
    private String deptName;
    @JsonProperty("user_id")
    private String userId;
    private String username;
    private String email;
    private String mobileNo;
    private String env;
}
