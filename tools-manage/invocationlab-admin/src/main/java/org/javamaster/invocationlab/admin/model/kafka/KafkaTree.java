package org.javamaster.invocationlab.admin.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yudong
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KafkaTree {
    private String kafkaNodeType;
    private String label;
    private Boolean isLeaf;
    private List<KafkaTree> children;
}
