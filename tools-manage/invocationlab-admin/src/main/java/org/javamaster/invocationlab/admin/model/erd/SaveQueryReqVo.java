package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

@Data
public class SaveQueryReqVo {
    private String projectId;
    private String sqlInfo;
    private String selectDB;
    private String title;
    private String treeNodeId;
}
