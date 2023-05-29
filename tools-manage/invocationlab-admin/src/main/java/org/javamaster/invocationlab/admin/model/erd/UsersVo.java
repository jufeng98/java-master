package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yudong
 */
@Data
public class UsersVo implements Serializable {
    private String id;
    private String username;
    private String nickname;
    private Integer gender;
    private String avatar;
    private String blog;
    private String company;
    private String location;
    private String email;
    private String pwd;
    private String salt;
    private Integer age;
    private String signature;
    private String title;
    private String classification;
    private String phone;
    private String deptId;
    private String deptName;
    private String wechatOpenid;
    private String qqOpenid;
    private String tenantId;
    private Boolean lockFlag;
    private Boolean delFlag;
    private Date createTime;
    private Date updateTime;
    private String creator;
    private String updater;
}
