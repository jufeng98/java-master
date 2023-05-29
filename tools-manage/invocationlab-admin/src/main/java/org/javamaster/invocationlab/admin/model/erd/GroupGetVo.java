package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GroupGetVo implements Serializable {
    private String id;
    private String configJSON;
    private String projectJSON;
    private String projectName;
    private String description;
    private String type;
    private String tags;
    private Object revision;
    private String delFlag;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;
}
