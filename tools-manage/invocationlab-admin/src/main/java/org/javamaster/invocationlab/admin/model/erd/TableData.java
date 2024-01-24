package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TableData {
    private Integer total;
    private Integer realTotal;
    private List<Map<String, Object>> records;
}
