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
public class EdgesBean {
    private String shape;
    private String relation;
    private String source;
    private String target;
    private String id;
    private List<ControlPointsBean> controlPoints;
    private Integer sourceAnchor;
    private Integer targetAnchor;
}
