package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/2/12
 */
@NoArgsConstructor
@Data
public class NodesBean {
    private String shape;
    private String title;
    private Boolean moduleName;
    private Integer x;
    private Integer y;
    private String id;
}
