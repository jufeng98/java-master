package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.util.Date;

@Data
public class ExecuteHistoryBean {
    private String sqlInfo;
    private String dbName;
    private Long duration;
    private Date createTime;
    private String creator;
    private String params;
}
