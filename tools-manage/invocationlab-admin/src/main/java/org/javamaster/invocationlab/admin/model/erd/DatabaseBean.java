package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/2/12
 */
@NoArgsConstructor
@Data
public class DatabaseBean {
    private String code;
    private String template;
    private Boolean fileShow;
    private Boolean defaultDatabase;
    private String createTableTemplate;
    private String deleteTableTemplate;
    private String rebuildTableTemplate;
    private String createFieldTemplate;
    private String updateFieldTemplate;
    private String deleteFieldTemplate;
    private String deleteIndexTemplate;
    private String createIndexTemplate;
    private String updateTableComment;
}
