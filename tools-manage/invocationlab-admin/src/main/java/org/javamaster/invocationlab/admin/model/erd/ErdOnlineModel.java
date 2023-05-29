package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/2/12
 */
@NoArgsConstructor
@Data
public class ErdOnlineModel {
    private ConfigJSONBean configJSON;
    private ProjectJSONBean projectJSON;
    private String projectName;
    private String type;
    private String id;
}
