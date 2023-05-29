package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yudong
 * @date 2023/2/12
 */
@NoArgsConstructor
@Data
public class EntitiesBean {
    private String title;
    private List<FieldsBean> fields;
    private List<IndexsBean> indexs;
    private List<?> headers;
    private String chnname;
    private String originalCreateTableSql;
}
