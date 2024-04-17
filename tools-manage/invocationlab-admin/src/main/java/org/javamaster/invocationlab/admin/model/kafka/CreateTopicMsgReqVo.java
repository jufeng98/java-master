package org.javamaster.invocationlab.admin.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTopicMsgReqVo {
    private String connectId;
    private String topic;
    private Integer numPartitions;
    private Short replicationFactor;
}
