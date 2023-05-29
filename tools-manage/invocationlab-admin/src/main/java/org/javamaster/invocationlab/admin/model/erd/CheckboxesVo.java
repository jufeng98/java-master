package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CheckboxesVo implements Serializable {
    private String menuName;
    private String menuId;
    private List<Long> defaultValue;
    private List<OperationsVo> operations;

}
