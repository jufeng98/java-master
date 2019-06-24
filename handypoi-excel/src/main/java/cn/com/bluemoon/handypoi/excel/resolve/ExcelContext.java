package cn.com.bluemoon.handypoi.excel.resolve;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author yudong
 * @date 2019/6/15
 */
public class ExcelContext {
    private Workbook workbook;
    private Sheet sheet;
    private Row row;

    public Workbook getWorkbook() {
        return workbook;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public Row getRow() {
        return row;
    }

    void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    void setRow(Row row) {
        this.row = row;
    }
}
