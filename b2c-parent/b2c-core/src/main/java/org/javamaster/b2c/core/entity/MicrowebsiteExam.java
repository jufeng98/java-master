package org.javamaster.b2c.core.entity;

import org.javamaster.b2c.core.enums.ExamStatusEnum;
import org.javamaster.b2c.core.enums.ExamTypeEnum;

import java.io.Serializable;
import java.util.Date;

/**
 * 考试表
 *
 * @author mybatis generator
 * @date 2019/06/13 09:37:47
 */
public class MicrowebsiteExam implements Serializable {
    /**
     * 考试编码
     */
    private String examCode;

    /**
     * 考试名称
     */
    private String examName;

    /**
     * 考试类型,1:独立考试;2:关联课程
     */
    private ExamTypeEnum examType;

    /**
     * 考试状态,1:未开始;2:待考试;3:考试中;4:已完成
     */
    private ExamStatusEnum examStatus;

    /**
     * 考试最后操作时间
     */
    private Date examOpDate;

    /**
     * 考试最后操作人
     */
    private String examOpUsername;

    /**
     * 删除标志,0:已删除;1:正常
     */
    private Byte delFlag;

    private static final long serialVersionUID = 3178964445235122176L;

    /**
     * 获取考试编码
     */
    public String getExamCode() {
        return examCode;
    }

    /**
     * 设置考试编码
     */
    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    /**
     * 获取考试名称
     */
    public String getExamName() {
        return examName;
    }

    /**
     * 设置考试名称
     */
    public void setExamName(String examName) {
        this.examName = examName;
    }

    /**
     * 获取考试类型,1:独立考试;2:关联课程
     */
    public ExamTypeEnum getExamType() {
        return examType;
    }

    /**
     * 设置考试类型,1:独立考试;2:关联课程
     *
     * @param examType
     */
    public void setExamType(ExamTypeEnum examType) {
        this.examType = examType;
    }

    /**
     * 获取考试状态,1:未开始;2:待考试;3:考试中;4:已完成
     */
    public ExamStatusEnum getExamStatus() {
        return examStatus;
    }

    /**
     * 设置考试状态,1:未开始;2:待考试;3:考试中;4:已完成
     *
     * @param examStatus
     */
    public void setExamStatus(ExamStatusEnum examStatus) {
        this.examStatus = examStatus;
    }

    public Date getExamOpDate() {
        return examOpDate;
    }

    public void setExamOpDate(Date examOpDate) {
        this.examOpDate = examOpDate;
    }

    public String getExamOpUsername() {
        return examOpUsername;
    }

    public void setExamOpUsername(String examOpUsername) {
        this.examOpUsername = examOpUsername;
    }

    /**
     * 获取删除标志,0:已删除;1:正常
     */
    public Byte getDelFlag() {
        return delFlag;
    }

    /**
     * 设置删除标志,0:已删除;1:正常
     */
    public void setDelFlag(Byte delFlag) {
        this.delFlag = delFlag;
    }
}