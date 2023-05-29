package org.javamaster.invocationlab.admin.service.creation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yudong
 */
@Data
public class InterfaceEntity {
    String key;

    String interfaceName;

    Set<String> serverIps = new HashSet<>();

    @JsonIgnore
    Class<?> interfaceClass;

    Set<String> methodNames = new HashSet<>();

    List<MethodEntity> methods = new ArrayList<>();

    String group;

    String version;

    long timeout;

    String registryBeanName;

    boolean check;

    int retries;
}
