package org.javamaster.spring.tools.manage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/3/3
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
}
