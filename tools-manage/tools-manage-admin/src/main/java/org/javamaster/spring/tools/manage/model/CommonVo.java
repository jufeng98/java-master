package org.javamaster.spring.tools.manage.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yudong
 * @date 2023/3/3
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommonVo extends ValueVo {
    private String connectId;
    private Integer redisDbIndex;
    private String redisKey;
    private String redisKeyBase64;
    private String fieldKey;
    private Double score;
    private String oldRedisKey;
    private String oldRedisValue;
}
