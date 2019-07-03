package cn.com.bluemoon.handypoi.excel.resolve;

import cn.com.bluemoon.handypoi.excel.annos.ExcelColumn;
import cn.com.bluemoon.handypoi.excel.annos.ExcelColumnDate;
import cn.com.bluemoon.handypoi.excel.annos.ExcelColumnMoney;
import cn.com.bluemoon.handypoi.excel.enums.ExcelType;
import cn.com.bluemoon.handypoi.excel.listener.RowReadListener;
import cn.com.bluemoon.handypoi.excel.model.BeanColumnField;
import cn.com.bluemoon.handypoi.excel.utils.ConvertUtils;
import cn.com.bluemoon.handypoi.excel.utils.StyleUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 读取excel
 *
 * @author yudong
 * @date 2019/6/9
 */
public class ExcelReader<T> {

    private ExcelContext context;

    private Workbook workbook;
    private Sheet sheet;
    /**
     * 头部标题行数
     */
    private int headerNum;

    /**
     * 尾部行数
     */
    private int footerNum;

    /**
     * 待转换的bean类型
     */
    private Class<T> beanClz;
    private Map<String, BeanColumnField> columnNameBeanFieldMap;
    /**
     * 收集读取结果的list
     */
    private List<T> resultList;

    private RowReadListener<T> rowReadListener = (T bean, ExcelContext context) -> true;

    /**
     * @param excelType   excel文件类型
     * @param inputStream 文件流
     * @param beanClz     待转换的bean类型
     * @param headerNum   头部标题行数
     * @param footerNum   尾部非数据行数
     */
    public ExcelReader(ExcelType excelType, InputStream inputStream, Class<T> beanClz, int headerNum, int footerNum) {
        try {
            if (ExcelType.XLS == excelType) {
                this.workbook = new HSSFWorkbook(inputStream);
            } else {
                this.workbook = new XSSFWorkbook(inputStream);
            }
            this.sheet = workbook.getSheetAt(0);
            this.headerNum = headerNum;
            this.footerNum = footerNum;
            this.beanClz = beanClz;
            init();
            context = new ExcelContext();
            context.setWorkbook(this.workbook);
            context.setSheet(this.sheet);
        } catch (Exception e) {
            throw new RuntimeException("construct workbook failed", e);
        } finally {
            if (this.workbook != null) {
                try {
                    this.workbook.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 设置监听器
     *
     * @param rowReadListener 行读取监听器
     */
    public void setRowReadListener(RowReadListener<T> rowReadListener) {
        this.rowReadListener = rowReadListener;
    }

    /**
     * 读取excel文件
     */
    public void read() {
        try {
            // 获取标题行
            Row headerRow = sheet.getRow(headerNum - 1);
            // 解析内容行
            resultList = IntStream.rangeClosed(headerNum, sheet.getLastRowNum() - footerNum)
                    .mapToObj(i -> {
                        Row row = sheet.getRow(i);
                        T bean;
                        try {
                            bean = beanClz.newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException("try to construct bean failed", e);
                        }
                        // 解析内容行的列信息
                        IntStream.range(headerRow.getFirstCellNum(), headerRow.getLastCellNum()).forEach(j -> {
                            try {
                                Cell headerCell = headerRow.getCell(j);
                                BeanColumnField beanColumnField = columnNameBeanFieldMap.get(headerCell.getStringCellValue());
                                Cell cell = row.getCell(j);
                                Field beanField = beanColumnField.getField();
                                Object beanValue;
                                if (beanField.getType() == String.class) {
                                    beanValue = cell.getStringCellValue();
                                } else if (Number.class.isAssignableFrom(beanField.getType())) {
                                    beanValue = ConvertUtils.numberType(beanField.getType(), cell);
                                } else if (beanField.getType().isPrimitive()) {
                                    double value = ConvertUtils.primitiveType(beanField.getType(), cell);
                                    Class<?> fieldTypeClz = beanField.getType();
                                    if (fieldTypeClz == long.class) {
                                        beanField.set(bean, (long) value);
                                    } else if (fieldTypeClz == double.class) {
                                        beanField.set(bean, value);
                                    } else if (fieldTypeClz == float.class) {
                                        beanField.set(bean, (float) value);
                                    } else {
                                        beanField.set(bean, (int) value);
                                    }
                                    return;
                                } else if (Date.class.isAssignableFrom(beanField.getType())) {
                                    beanValue = ConvertUtils.dateType(beanField.getType(), cell, beanColumnField.getDatePattern());
                                } else {
                                    throw new RuntimeException("unsupported type:" + beanField.getType());
                                }
                                beanField.set(bean, beanValue);
                            } catch (Exception e) {
                                throw new RuntimeException("fill bean property value failed", e);
                            }
                        });
                        context.setRow(row);
                        if (rowReadListener.accept(bean, context)) {
                            return bean;
                        } else {
                            return null;
                        }

                    })
                    .filter(bean -> bean != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("resolved excel failed", e);
        } finally {
            if (this.workbook != null) {
                try {
                    this.workbook.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 获取解析结果list
     *
     * @return
     */
    public List<T> getResultList() {
        return resultList;
    }

    private void init() {
        Field[] fields = beanClz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        List<BeanColumnField> beanColumnFields = Arrays.stream(fields)
                .map(field -> {
                    ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
                    if (excelColumn == null) {
                        return null;
                    }
                    Class<?> fieldType = field.getType();
                    BeanColumnField beanColumnField = new BeanColumnField();
                    beanColumnField.setField(field);
                    beanColumnField.setColumnName(excelColumn.columnName());

                    ExcelColumnMoney excelColumnMoney = field.getAnnotation(ExcelColumnMoney.class);
                    if (excelColumnMoney != null) {
                        beanColumnField.setMoneyField(true);
                        beanColumnField.setMoneyUnit(excelColumnMoney.moneyUnit());
                        return beanColumnField;
                    }

                    if (fieldType == Date.class || fieldType == Timestamp.class) {
                        beanColumnField.setDateField(true);
                        ExcelColumnDate excelColumnDate = field.getAnnotation(ExcelColumnDate.class);
                        if (excelColumnDate != null) {
                            beanColumnField.setDatePattern(excelColumnDate.datePattern());
                        } else {
                            beanColumnField.setDatePattern(StyleUtils.DEFAULT_DATE_PATTERN);
                        }
                    }
                    return beanColumnField;
                })
                .filter(beanColumnField -> beanColumnField != null)
                .collect(Collectors.toList());
        columnNameBeanFieldMap = new HashMap<>(beanColumnFields.size(), 1);
        for (BeanColumnField columnField : beanColumnFields) {
            columnNameBeanFieldMap.put(columnField.getColumnName()[columnField.getColumnName().length - 1], columnField);
        }
    }
}
