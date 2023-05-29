package org.javamaster.invocationlab.admin.model.erd;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yudong
 * @date 2019/7/8
 */
@Data
@AllArgsConstructor
public class Table {
    private String name;
    private String remarks;

    @Override
    public String toString() {
        return name + "  " + remarks;
    }

}
