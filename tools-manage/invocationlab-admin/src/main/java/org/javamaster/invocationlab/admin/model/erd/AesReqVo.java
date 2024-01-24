package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.io.Serializable;

@Data
public class AesReqVo implements Serializable {
    private String projectId;
    private String opType;
    private String value;
}
