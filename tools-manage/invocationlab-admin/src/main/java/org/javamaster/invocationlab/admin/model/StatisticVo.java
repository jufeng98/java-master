package org.javamaster.invocationlab.admin.model;

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
    private Integer yesterday;
    private Integer total;
    private Integer month;
    private Integer today;
}
