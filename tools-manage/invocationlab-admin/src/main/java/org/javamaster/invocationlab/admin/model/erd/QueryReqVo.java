package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

@Data
public class QueryReqVo {
    private String projectId;
    private Boolean isLeaf;
    private String title;
    private String parentId;
}
