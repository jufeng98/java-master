package org.javamaster.b2c.core.model.vo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.javamaster.b2c.core.annos.EnumCodeRange;
import org.javamaster.b2c.core.enums.ExamTypeEnum;

import javax.validation.constraints.NotNull;

/**
 * @author yudong
 * @date 2019/6/10
 */
public class GetExamListReqVo {
    @NotNull(message = "考试类型不能为空")
    @EnumCodeRange(min = 1, max = 2, message = "考试类型须位于{min}和{max}之间")
    private ExamTypeEnum examType;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public ExamTypeEnum getExamType() {
        return examType;
    }

    public void setExamType(ExamTypeEnum examType) {
        this.examType = examType;
    }
}
