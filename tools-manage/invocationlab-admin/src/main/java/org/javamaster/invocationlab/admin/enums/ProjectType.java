package org.javamaster.invocationlab.admin.enums;

/**
 * @author yudong
 * @date 2022/11/8
 */
public enum ProjectType {
    PERSONAL(1),
    GROUP(2),
    ;

    public final int type;

    ProjectType(int type) {
        this.type = type;
    }

    public static ProjectType getByType(Integer type) {
        if (type == null) {
            return GROUP;
        }
        for (ProjectType value : ProjectType.values()) {
            if (value.type == type) {
                return value;
            }
        }
        return GROUP;
    }
}
