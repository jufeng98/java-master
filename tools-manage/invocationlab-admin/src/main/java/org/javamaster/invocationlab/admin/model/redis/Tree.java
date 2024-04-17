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
@NoArgsConstructor
@AllArgsConstructor
public class Tree {
    private Integer redisDbIndex;
    private Long keyCount;
    private String label;
    private String labelBase64;
    private Boolean isLeaf;
    private Class<?> labelClass;
    private Integer typeLength;
}
