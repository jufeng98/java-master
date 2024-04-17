package org.javamaster.invocationlab.admin.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendTopicMsgReqVo {
    private String connectId;
    private String topic;
    private Integer partition;
    private String headers;
    private String key;
    private String value;
}
