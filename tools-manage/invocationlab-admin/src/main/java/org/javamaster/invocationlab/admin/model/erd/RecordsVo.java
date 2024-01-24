package org.javamaster.invocationlab.admin.model.erd;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yudong
 * @date 2023/2/14
 */
@NoArgsConstructor
@Data
public class RecordsVo {
    private String id;
    private String projectName;
    private String tags;
    private String description;
    private Integer type;
    private String creator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private String updater;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
