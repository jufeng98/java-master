package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * @author yudong
 */
@Data
public class UserVo implements Serializable {
    private String orgId;
    private String orgName;
    private Set<String> roleIds;
    private String password;
}
