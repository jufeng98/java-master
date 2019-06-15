package cn.com.bluemoon.handypoi.excel.resolve;

import cn.com.bluemoon.handypoi.excel.model.BeanColumnField;
import cn.com.bluemoon.handypoi.excel.model.Style;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;

/**
 * @author yudong
 * @date 2019/6/9
 */
public class SheetInfo<T> {
    /**
     * 待写入的list
     */
    private List<T> dataList;
    /**
     * 待写入的对象class类型
     */
    private Class<T> dataClz;
    /**
     * 工作簿名字
     */
    private String sheetName;
    /**
     * 头部标题行数
     */
    private int headerNum;

    private Sheet sheet;
    private List<BeanColumnField> beanColumnFields;
    /**
     * 是否多级表头
     */
    private boolean multiHeader;


    private CellStyle headerStyle;
    private CellStyle contentStyle;

    /**
     * @param dataList  待写入的bean的list
     * @param dataClz   待写入的bean类型
     * @param sheetName 工作簿名字
     * @param headerNum 头部标题行数
     */
    public SheetInfo(List<T> dataList, Class<T> dataClz, String sheetName, int headerNum) {
        this.dataList = dataList;
        this.dataClz = dataClz;
        this.sheetName = sheetName;
        this.headerNum = headerNum;
        if (this.headerNum > 1) {
            this.multiHeader = true;
        }
    }

    public void setHeaderStyle(Style style) {

    }

    void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    void setHeaderStyle(CellStyle headerStyle) {
        this.headerStyle = headerStyle;
    }

    void setContentStyle(CellStyle contentStyle) {
        this.contentStyle = contentStyle;
    }

    void setBeanColumnFields(List<BeanColumnField> beanColumnFields) {
        this.beanColumnFields = beanColumnFields;
    }

    List<T> getDataList() {
        return dataList;
    }

    Class<T> getDataClz() {
        return dataClz;
    }

    String getSheetName() {
        return sheetName;
    }

    int getHeaderNum() {
        return headerNum;
    }

    Sheet getSheet() {
        return sheet;
    }

    List<BeanColumnField> getBeanColumnFields() {
        return beanColumnFields;
    }

    boolean isMultiHeader() {
        return multiHeader;
    }

    CellStyle getHeaderStyle() {
        return headerStyle;
    }

    CellStyle getContentStyle() {
        return contentStyle;
    }
}
