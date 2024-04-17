package org.javamaster.invocationlab.admin.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicMsg {
    private Integer partition;
    private Long offset;
    private Date timestamp;
    private Object key;
    private Object value;
    private String headers;
}
