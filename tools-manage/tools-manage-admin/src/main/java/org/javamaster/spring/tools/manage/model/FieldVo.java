package org.javamaster.spring.tools.manage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/3/3
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
