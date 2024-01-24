package org.javamaster.invocationlab.admin.service.invocation.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yudong
 */
@Setter
@Getter
public class PostmanDubboRequest implements PostmanRequest {
    String dubboParam;
    String cluster;
    String serviceName;
    String group;
    String interfaceName;
    String version;
    String methodName;
    String dubboIp;

}
