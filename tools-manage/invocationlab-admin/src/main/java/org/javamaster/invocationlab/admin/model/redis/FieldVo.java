package org.javamaster.invocationlab.admin.model.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldVo {
    private Integer fieldIndex;
    private String fieldKey;
    private String fieldValue;
    private Double fieldScore;
    private Integer fieldValueSize;
}
