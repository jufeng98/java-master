package cn.com.bluemoon.handypoi.excel.model;

import cn.com.bluemoon.handypoi.excel.enums.MoneyUnit;
import org.apache.poi.ss.usermodel.CellStyle;

import java.lang.reflect.Field;

/**
 * bean属性相关的信息
 *
 * @author yudong
 * @date 2019/6/9
 */
public class BeanColumnField {
    private Field field;
    private String[] columnName;
    private int columnOrder;
    private int columnWidth;
    private int columnIndex;

    private boolean dateField;
    private String datePattern;

    private boolean moneyField;
    private MoneyUnit moneyUnit;

    private boolean decimalField;

    private CellStyle cellStyle;


    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public MoneyUnit getMoneyUnit() {
        return moneyUnit;
    }

    public void setMoneyUnit(MoneyUnit moneyUnit) {
        this.moneyUnit = moneyUnit;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String[] getColumnName() {
        return columnName;
    }

    public void setColumnName(String[] columnName) {
        this.columnName = columnName;
    }

    public int getColumnOrder() {
        return columnOrder;
    }

    public void setColumnOrder(int columnOrder) {
        this.columnOrder = columnOrder;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public boolean isDateField() {
        return dateField;
    }

    public void setDateField(boolean dateField) {
        this.dateField = dateField;
    }

    public boolean isMoneyField() {
        return moneyField;
    }

    public void setMoneyField(boolean moneyField) {
        this.moneyField = moneyField;
    }

    public boolean isDecimalField() {
        return decimalField;
    }

    public void setDecimalField(boolean decimalField) {
        this.decimalField = decimalField;
    }

    public CellStyle getCellStyle() {
        return cellStyle;
    }

    public void setCellStyle(CellStyle cellStyle) {
        this.cellStyle = cellStyle;
    }
}
