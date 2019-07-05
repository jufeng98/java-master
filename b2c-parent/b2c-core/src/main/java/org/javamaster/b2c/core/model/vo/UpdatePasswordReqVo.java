package org.javamaster.b2c.core.model.vo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.javamaster.b2c.core.entity.SysUser;

import javax.validation.constraints.NotBlank;

/**
 * Created on 2018/12/10.<br/>
 *
 * @author yudong
 */
public class UpdatePasswordReqVo extends SysUser {

    private static final long serialVersionUID = -1353706614638513233L;
    @NotBlank
    private String newPassword;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(ToStringStyle.JSON_STYLE);
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
