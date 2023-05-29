package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class SaveCheckedOperationsReqVo implements Serializable {
    private String roleId;
    private String projectId;
    private Set<Long> checkedKeys;
}
