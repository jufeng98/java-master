package org.javamaster.invocationlab.admin.model.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionVo {
    private String connectId;
    private String nodes;
    private String name;
    private String host;
    private Integer port;
    private String user;
    private String password;
    private Long createTime;
}
