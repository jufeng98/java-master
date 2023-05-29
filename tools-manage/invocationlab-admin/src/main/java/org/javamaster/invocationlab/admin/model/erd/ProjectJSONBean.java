package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yudong
 * @date 2023/2/12
 */
@NoArgsConstructor
@Data
public class ProjectJSONBean {
    private List<ModulesBean> modules;
    private ProfileBean profile;
    private DataTypeDomainsBean dataTypeDomains;
}
