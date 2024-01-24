package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class SqlExecResVo {
    private Set<String> columns;
    private Map<String, Column> tableColumns;

    private Integer queryKey;

    private Integer page;
    private Integer pageSize;

    private Boolean showPagination;
    private String tableName;
    private List<String> primaryKeys;

    private TableData tableData;
}
