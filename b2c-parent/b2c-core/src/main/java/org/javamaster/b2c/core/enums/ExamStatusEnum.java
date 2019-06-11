package org.javamaster.b2c.core.enums;

/**
 * @author yudong
 * @date 2019/6/10
 */
public enum ExamStatusEnum implements EnumBase {
    EXAM_NOT_START(1, "未开始"),
    NEED_EXAM(2, "待考试"),
    IN_EXAM(3, "考试中"),
    FINISH_EXAM(4, "已完成");
    private final int code;
    private final String msg;

    ExamStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
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
