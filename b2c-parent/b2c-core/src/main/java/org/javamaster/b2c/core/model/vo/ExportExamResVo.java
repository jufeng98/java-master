package org.javamaster.b2c.core.model.vo;

import cn.com.bluemoon.handypoi.excel.annos.ExcelColumn;

/**
 * @author yudong
 * @date 2019/7/11
 */
public class ExportExamResVo {
    @ExcelColumn(columnName = "考试编码")
    private String examCode;
    @ExcelColumn(columnName = "考试名称")
    private String examName;
    @ExcelColumn(columnName = "考试类型")
    private String examTypeName;
    @ExcelColumn(columnName = "考试状态")
    private String examStatusName;

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamTypeName() {
        return examTypeName;
    }

    public void setExamTypeName(String examTypeName) {
        this.examTypeName = examTypeName;
    }

    public String getExamStatusName() {
        return examStatusName;
    }

    public void setExamStatusName(String examStatusName) {
        this.examStatusName = examStatusName;
    }
}
