package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/2/15
 */
@NoArgsConstructor
@Data
public class PropertiesBean {
    private String driver_class_name;
    private String url;
    private String password;
    private String username;
    private String cipherType;
    private String cipherKey;

    public String unique() {
        return url + ":" + username + ":" + password + ":" + driver_class_name;
    }
}
