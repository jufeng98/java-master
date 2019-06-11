package org.javamaster.b2c.core.model;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author yudong
 * @date 2019/6/10
 */
public class AuthAuthority implements GrantedAuthority {

    private String authorityCode;

    private String authorityName;

    public String getAuthorityCode() {
        return authorityCode;
    }

    public void setAuthorityCode(String authorityCode) {
        this.authorityCode = authorityCode == null ? null : authorityCode.trim();
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName == null ? null : authorityName.trim();
    }

    @Override
    public String getAuthority() {
        return authorityName;
    }
}