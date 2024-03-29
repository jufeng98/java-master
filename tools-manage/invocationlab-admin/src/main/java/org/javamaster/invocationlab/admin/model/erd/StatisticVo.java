package org.javamaster.invocationlab.admin.model.erd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/2/14
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class StatisticVo {
    private Integer yesterday = 8;
    private Integer total;
    private Integer month = 18;
    private Integer today = 8;
}
