package org.javamaster.b2c.core.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yudong
 * @date 2019/6/10
 */
public enum ExamTypeEnum implements EnumBase {
    UNKNOWN(0, ""),
    EXAM_INDEPENDENT(1, "独立考试"),
    EXAM_COURSE_SUITE(2, "关联课程");
    private final int code;
    private final String msg;
    private static final Map<Integer, ExamTypeEnum> MAP;

    static {
        MAP = new HashMap<>(3, 1);
        for (ExamTypeEnum value : ExamTypeEnum.values()) {
            MAP.put(value.getCode(), value);
        }
    }

    ExamTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ExamTypeEnum getEnumByCode(Integer code) {
        return MAP.getOrDefault(code, UNKNOWN);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
