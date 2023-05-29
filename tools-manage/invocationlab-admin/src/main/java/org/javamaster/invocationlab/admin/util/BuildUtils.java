package org.javamaster.invocationlab.admin.util;

/**
 * @author yudong
 */
public class BuildUtils {
    private final static String SPLITTER = "_";

    public static String buildServiceKey(String cluster, String serviceName) {
        return cluster + SPLITTER + serviceName;
    }

    public static String buildInterfaceKey(String group, String interfaceName, String version) {
        String versionAppend = version;
        if (versionAppend == null || versionAppend.isEmpty()) {
            versionAppend = Constant.DEFAULT_VERSION;
        }
        return group + SPLITTER + interfaceName + SPLITTER + versionAppend;
    }

    public static String getGroupByInterfaceKey(String interfaceKey) {
        return interfaceKey.split(SPLITTER)[0];
    }

    public static String getInterfaceNameByInterfaceKey(String interfaceKey) {
        return interfaceKey.split(SPLITTER)[1];
    }

    public static String getVersionByInterfaceKey(String interfaceKey) {
        return interfaceKey.split(SPLITTER)[2];
    }

    public static String getJavaMethodName(String methodName) {
        return methodName.split("\\(")[0];
    }

    public static String buildMethodNameKey(String cluster,
                                            String serviceName,
                                            String group,
                                            String interfaceName,
                                            String version,
                                            String methodName) {
        String serviceKey = BuildUtils.buildServiceKey(cluster, serviceName);
        String interfaceKey = BuildUtils.buildInterfaceKey(group, interfaceName, version);
        return serviceKey + SPLITTER + interfaceKey + SPLITTER + methodName;
    }

    public static String getMethodNameKey(String cluster,
                                          String serviceName,
                                          String interfaceKey,
                                          String methodName) {
        String serviceKey = BuildUtils.buildServiceKey(cluster, serviceName);
        return serviceKey + SPLITTER + interfaceKey + SPLITTER + methodName;
    }

    /**
     * 把ip地址拼接成zk地址
     * zookeeper://127.0.0.1:2181?backup=127.0.0.2:2181,127.0.0.3:2181
     */
    public static String buildZkUrl(final String zk) {
        StringBuilder zkRegis;
        String prefix = "zookeeper://";
        if (zk.contains(",")) {
            String[] zs = zk.split(",");
            zkRegis = new StringBuilder(prefix + zs[0]);
            if (zs.length > 1) {
                zkRegis.append("?backup=");
                for (int index = 1; index < zs.length; index++) {
                    zkRegis.append(zs[index]);
                    if (index < zs.length - 1) {
                        zkRegis.append(",");
                    }
                }
            }
        } else {
            zkRegis = new StringBuilder(prefix + zk);
        }
        return zkRegis.toString();
    }
}
