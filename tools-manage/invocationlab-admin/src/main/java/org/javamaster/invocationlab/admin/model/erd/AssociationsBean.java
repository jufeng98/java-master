package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/2/12
 */
@NoArgsConstructor
@Data
public class AssociationsBean {
    private String relation;
    private FromBean from;
    private ToBean to;
}
