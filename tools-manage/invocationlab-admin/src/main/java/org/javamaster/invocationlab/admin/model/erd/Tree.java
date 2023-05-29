package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yudong
 * @date 2023/2/16
 */
@NoArgsConstructor
@Data
public class Tree {

    private String id;
    private String key;
    private String title;
    private String label;
    private String value;
    private Boolean isLeaf;
    private String parentId;
    private Object parentKey;
    private String sqlInfo;
    private String selectDB;
    private List<String> parentKeys;
    private List<Tree> children;

}
