package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/2/12
 */
@NoArgsConstructor
@Data
public class FieldsBean {
    private String chnname;
    private String name;
    private String typeName;
    private String type;
    private String dataType;
    private String remark;
    private Boolean pk;
    private Boolean notNull;
    private Boolean autoIncrement;
    private Boolean relationNoShow;
    private String defaultValue;
    private String uiHint;
}
