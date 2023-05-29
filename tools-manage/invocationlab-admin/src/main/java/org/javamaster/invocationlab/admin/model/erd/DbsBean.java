package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/2/15
 */
@NoArgsConstructor
@Data
public class DbsBean {

    private String name;
    private String select;
    private String key;
    private Boolean defaultDB;
    private PropertiesBean properties;

}
