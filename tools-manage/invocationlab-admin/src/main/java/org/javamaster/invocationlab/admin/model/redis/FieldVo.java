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
    private Class<?> fieldKeyClazz;
    private Boolean fieldKeyJdkSerialize;

    private String fieldValue;
    private Boolean fieldValueJdkSerialize;
    private Class<?> fieldValueClazz;
    private Double fieldScore;
    private Integer fieldValueSize;
}
