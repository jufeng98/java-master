package org.javamaster.invocationlab.admin.enums;

/**
 * @author yudong
 * @date 2022/11/8
 */
public enum RegisterCenterType {
    ZK(1),
    EUREKA(2),
    ;

    public final int type;

    RegisterCenterType(int type) {
        this.type = type;
    }

    public static RegisterCenterType getByType(Integer type) {
        if (type == null) {
            return ZK;
        }
        for (RegisterCenterType value : RegisterCenterType.values()) {
            if (value.type == type) {
                return value;
            }
        }
        return null;
    }
}
