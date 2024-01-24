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
public class ModulesBean {
    private String name;
    private String chnname;
    private Integer sort;
    private List<EntitiesBean> entities;
    private GraphCanvasBean graphCanvas;
    private List<AssociationsBean> associations;
}
