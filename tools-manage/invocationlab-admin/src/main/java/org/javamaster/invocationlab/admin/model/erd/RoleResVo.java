package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleResVo implements Serializable {
    private Long id;
    private String roleId;
    private String projectId;
    private String roleName;
    private String roleCode;
}
