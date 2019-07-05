package org.javamaster.b2c.core.model.vo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.javamaster.b2c.core.entity.SysUser;
import org.javamaster.b2c.core.model.Page;

import javax.validation.constraints.NotNull;

/**
 * @author yudong
 * @date 2019/7/5
 */
public class FindUsersReqVo {
    @NotNull
    private SysUser sysUser;
    @NotNull
    private Page page;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(ToStringStyle.JSON_STYLE);
    }

    public SysUser getSysUser() {
        return sysUser;
    }

    public void setSysUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }
}
