package org.javamaster.invocationlab.admin.model.erd;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

@Data
public class CommonErdVo {
    private String projectId;
    private String selectDB;
    private String tableName;
    private String sql;
    private String queryId;
    private String type;

    private Boolean explain;

    private Integer page;
    private Integer pageSize;

    private Boolean isExport;

    private JSONArray rows;
}
