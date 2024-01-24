package org.javamaster.invocationlab.admin.model.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yudong
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValueVo {
    private String redisValue;
    private Integer redisValueSize;
    private Boolean redisValueJdkSerialize;
    private Class<?> redisValueClazz;
    private String redisKeyBase64;

    private Long redisKeyTtl;
    private String redisKey;
    private Class<?> redisKeyClazz;
    private Boolean redisKeyJdkSerialize;
    private String redisKeyType;

    private List<FieldVo> fieldVos;
    private Integer fieldCount;
}
