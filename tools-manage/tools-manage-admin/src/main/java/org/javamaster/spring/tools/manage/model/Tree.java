package org.javamaster.spring.tools.manage.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author yudong
 * @date 2023/3/3
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
