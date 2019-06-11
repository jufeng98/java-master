package org.javamaster.b2c.core.entity;

import org.javamaster.b2c.core.enums.ExamStatusEnum;
import org.javamaster.b2c.core.enums.ExamTypeEnum;

public class MicrowebsiteExam {
    private String examCode;

    private String examName;

    private ExamTypeEnum examType;

    private ExamStatusEnum examStatus;

    private Boolean delFlag;

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode == null ? null : examCode.trim();
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName == null ? null : examName.trim();
    }

    public ExamTypeEnum getExamType() {
        return examType;
    }

    public void setExamType(ExamTypeEnum examType) {
        this.examType = examType;
    }

    public ExamStatusEnum getExamStatus() {
        return examStatus;
    }

    public void setExamStatus(ExamStatusEnum examStatus) {
        this.examStatus = examStatus;
    }

    public Boolean getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Boolean delFlag) {
        this.delFlag = delFlag;
    }
}