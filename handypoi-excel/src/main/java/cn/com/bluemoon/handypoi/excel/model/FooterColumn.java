package cn.com.bluemoon.handypoi.excel.model;

public class FooterColumn {
    private Object columnValue;
    private int mergeColumnNum;
    private boolean func;
    private int mergeRowNum;

    public FooterColumn(Object columnValue) {
        this.columnValue = columnValue;
    }

    public FooterColumn(Object columnValue, boolean func) {
        this.columnValue = columnValue;
        this.func = func;
    }

    public FooterColumn(Object columnValue, int mergeColumnNum) {
        this.columnValue = columnValue;
        this.mergeColumnNum = mergeColumnNum;
    }

    public Object getColumnValue() {
        return columnValue;
    }

    public void setColumnValue(Object columnValue) {
        this.columnValue = columnValue;
    }

    public boolean isFunc() {
        return func;
    }

    public void setFunc(boolean func) {
        this.func = func;
    }

    public int getMergeColumnNum() {
        return mergeColumnNum;
    }

    public void setMergeColumnNum(int mergeColumnNum) {
        this.mergeColumnNum = mergeColumnNum;
    }

}
