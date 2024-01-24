package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.util.List;

@Data
public class SortModuleReqVo {
    private String projectId;
    private List<SortModuleVo> sortModuleVos;
}
