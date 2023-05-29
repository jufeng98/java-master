package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.io.Serializable;

@Data
public class OperationsVo implements Serializable {
    private String name;
    private Long value;
}
