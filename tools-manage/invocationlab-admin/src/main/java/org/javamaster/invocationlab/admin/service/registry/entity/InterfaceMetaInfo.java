package org.javamaster.invocationlab.admin.service.registry.entity;

import com.google.common.collect.Sets;
import lombok.Data;

import java.util.Set;

/**
 * @author yudong
 * 接口及接口包含的方法及参数等元数据,从zk获取
 * 如果从其他地方拉取这个数据类型应该是兼容的
 */
@Data
public class InterfaceMetaInfo {
    String applicationName;
    String group;
    String version;
    String interfaceName;
    /**
     * 这个是zk拼接的完整地址:dubbo://192.....
     */
    String serviceAddr;
    Set<String> methodNames = Sets.newHashSet();
    Set<String> serverIps = Sets.newHashSet();
}
