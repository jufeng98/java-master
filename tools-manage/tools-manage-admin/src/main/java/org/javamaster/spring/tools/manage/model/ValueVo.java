package org.javamaster.spring.tools.manage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yudong
 * @date 2023/3/3
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValueVo {
    private String redisValue;
    private Integer redisValueSize;
    private String redisKeyTtl;
    private String redisKeyType;
    private List<FieldVo> fieldVos;
    private Integer fieldCount;
}
