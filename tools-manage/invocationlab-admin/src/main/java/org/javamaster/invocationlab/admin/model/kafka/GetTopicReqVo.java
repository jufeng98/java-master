package org.javamaster.invocationlab.admin.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetTopicReqVo {
    private Integer partition;
    private Long offset;
    private String value;
    private Long timestamp;
}
