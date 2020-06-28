package org.javamaster.b2c.core.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 考试表
 * 
 * @author mybatis generator
 * @date 2019/07/11 14:41:41
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
    private Byte examType;

    /**
     * 考试状态,1:未开始;2:待考试;3:考试中;4:已完成
     */
    private Byte examStatus;

    /**
     * 考试最后操作日期
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

    private static final long serialVersionUID = 1478291352648552448L;

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
    public Byte getExamType() {
        return examType;
    }

    /**
     * 设置考试类型,1:独立考试;2:关联课程
     */
    public void setExamType(Byte examType) {
        this.examType = examType;
    }

    /**
     * 获取考试状态,1:未开始;2:待考试;3:考试中;4:已完成
     */
    public Byte getExamStatus() {
        return examStatus;
    }

    /**
     * 设置考试状态,1:未开始;2:待考试;3:考试中;4:已完成
     */
    public void setExamStatus(Byte examStatus) {
        this.examStatus = examStatus;
    }

    /**
     * 获取考试最后操作日期
     */
    public Date getExamOpDate() {
        return examOpDate;
    }

    /**
     * 设置考试最后操作日期
     */
    public void setExamOpDate(Date examOpDate) {
        this.examOpDate = examOpDate;
    }

    /**
     * 获取考试最后操作人
     */
    public String getExamOpUsername() {
        return examOpUsername;
    }

    /**
     * 设置考试最后操作人
     */
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