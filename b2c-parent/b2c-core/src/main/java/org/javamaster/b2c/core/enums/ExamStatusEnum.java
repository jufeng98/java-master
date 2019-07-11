package org.javamaster.b2c.core.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yudong
 * @date 2019/6/10
 */
public enum ExamStatusEnum implements EnumBase {
    UNKNOWN(0, ""),
    EXAM_NOT_START(1, "未开始"),
    NEED_EXAM(2, "待考试"),
    IN_EXAM(3, "考试中"),
    FINISH_EXAM(4, "已完成");
    private final int code;
    private final String msg;
    private static final Map<Integer, ExamStatusEnum> MAP;

    static {
        MAP = new HashMap<>(3, 1);
        for (ExamStatusEnum value : ExamStatusEnum.values()) {
            MAP.put(value.getCode(), value);
        }
    }

    ExamStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ExamStatusEnum getEnumByCode(Integer code) {
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
