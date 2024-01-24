package org.javamaster.invocationlab.admin.model.redis;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yudong
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommonRedisVo extends ValueVo {
    private String connectId;
    private Integer redisDbIndex;

    private String fieldKey;
    private Boolean fieldKeyJdkSerialize;
    private Double score;
    private String oldRedisKey;
    private String oldRedisValue;
}
