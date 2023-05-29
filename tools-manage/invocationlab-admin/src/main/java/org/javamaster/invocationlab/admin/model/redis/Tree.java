package org.javamaster.invocationlab.admin.model.redis;

import lombok.Builder;
import lombok.Data;

/**
 * @author yudong
 */
@Data
@Builder
public class Tree {
    private Integer redisDbIndex;
    private Long keyCount;
    private String label;
    private String labelBase64;
    private Boolean isLeaf;
}
