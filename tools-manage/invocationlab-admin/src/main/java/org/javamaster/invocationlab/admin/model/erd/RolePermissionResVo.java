package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RolePermissionResVo implements Serializable {
    private Integer loginRole;
    private List<String> permission;
}
