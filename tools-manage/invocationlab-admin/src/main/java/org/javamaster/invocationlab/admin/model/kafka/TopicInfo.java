package org.javamaster.invocationlab.admin.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicInfo {
    private int partition;
    private String leader;
    private List<String> replicas;
    private List<String> isr;
}
