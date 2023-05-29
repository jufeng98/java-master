package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yudong
 */
@Data
public class UsersResVo implements Serializable {
    private Integer total;
    private Integer size;
    private Integer current;
    private boolean searchCount;
    private Integer pages;
    private List<UsersVo> records;
    private List<Object> orders;

}
