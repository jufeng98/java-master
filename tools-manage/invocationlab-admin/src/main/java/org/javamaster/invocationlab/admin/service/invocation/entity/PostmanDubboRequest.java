package org.javamaster.invocationlab.admin.service.invocation.entity;

/**
 * @author yudong
 */
public class PostmanDubboRequest implements PostmanRequest {
    String dubboParam;
    String cluster;
    String serviceName;
    String group;
    String interfaceName;
    String version;
    String methodName;
    String dubboIp;

    public String getDubboParam() {
        return dubboParam;
    }

    public void setDubboParam(String dubboParam) {
        this.dubboParam = dubboParam;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDubboIp() {
        return dubboIp;
    }

    public void setDubboIp(String dubboIp) {
        this.dubboIp = dubboIp;
    }
}
