package cn.com.bluemoon.handypoi.excel.resolve;

import cn.com.bluemoon.handypoi.excel.annos.ExcelColumn;
import cn.com.bluemoon.handypoi.excel.annos.ExcelColumnDate;
import cn.com.bluemoon.handypoi.excel.annos.ExcelColumnDecimal;
import cn.com.bluemoon.handypoi.excel.annos.ExcelColumnMoney;
import cn.com.bluemoon.handypoi.excel.enums.ExcelType;
import cn.com.bluemoon.handypoi.excel.enums.MoneyUnit;
import cn.com.bluemoon.handypoi.excel.function.TripleConsumer;
import cn.com.bluemoon.handypoi.excel.listener.RowWriteListener;
import cn.com.bluemoon.handypoi.excel.model.BeanColumnField;
import cn.com.bluemoon.handypoi.excel.model.FooterColumn;
import cn.com.bluemoon.handypoi.excel.model.FooterRow;
import cn.com.bluemoon.handypoi.excel.utils.CellUtils;
import cn.com.bluemoon.handypoi.excel.utils.StyleUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 写入excel
 *
 * @author yudong
 * @date 2019/6/9
 */
public class ExcelWriter<T> {

    private ExcelContext context;

    private Workbook workbook;
    /**
     * 待写入的sheet信息
     */
    private List<SheetInfo> sheetInfoList = new ArrayList<>(6);
    /**
     * 行写入监听器
     */
    private RowWriteListener<T> rowWriteListener = new RowWriteListener<T>() {
    };

    /**
     * @param excelType excel文件类型
     */
    public ExcelWriter(ExcelType excelType) {
        if (ExcelType.XLS == excelType) {
            this.workbook = new HSSFWorkbook();
        } else {
            this.workbook = new SXSSFWorkbook(500);
        }
        context = new ExcelContext();
        context.setWorkbook(this.workbook);
    }

    /**
     * 添加待处理的sheet信息
     *
     * @param sheetInfo
     */
    public void addSheetInfo(SheetInfo sheetInfo) {
        sheetInfoList.add(sheetInfo);
    }

    /**
     * 设置监听器
     *
     * @param rowWriteListener
     */
    public void setRowWriteListener(RowWriteListener rowWriteListener) {
        this.rowWriteListener = rowWriteListener;
    }

    /**
     * 完成sheet信息添加
     *
     * @return
     */
    public ExcelWriterService addSheetFinish() {
        return new ExcelWriterImpl();
    }


    private class ExcelWriterImpl implements ExcelWriterService {

        public ExcelWriterImpl() {
            sheetInfoList.forEach(sheetInfo -> init(sheetInfo));
        }

        @Override
        public Workbook getWorkBook() {
            sheetInfoList.forEach(sheetInfo -> {
                fillHeaderInfo(sheetInfo);
                fillContentInfo(sheetInfo);
                fillFooterInfo(sheetInfo);
            });
            return workbook;
        }

        @Override
        public byte[] getBytes() {
            getWorkBook();
            byte[] bytes;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
                bytes = outputStream.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("resolve workbook to stream failed", e);
            } finally {
                try {
                    workbook.close();
                } catch (IOException e) {
                }
            }
            return bytes;
        }

        /**
         * 填充头部标题行信息
         *
         * @param sheetInfo
         */
        private void fillHeaderInfo(SheetInfo sheetInfo) {
            if (sheetInfo.isMultiHeader()) {
                calculateMergedRegion(sheetInfo);
            }
            for (int i = 0; i < sheetInfo.getHeaderNum(); i++) {
                Row headerRow = sheetInfo.getSheet().createRow(i);
                context.setSheet(sheetInfo.getSheet());
                context.setRow(headerRow);
                rowWriteListener.headerBeforeWriteAction(context);
                List<BeanColumnField> beanColumnFields = sheetInfo.getBeanColumnFields();
                for (int j = 0; j < beanColumnFields.size(); j++) {
                    BeanColumnField beanColumnField = beanColumnFields.get(j);
                    Cell headerCell = headerRow.createCell(j);
                    CellUtils.fillStrCell(headerCell, beanColumnField.getColumnName()[i],
                            beanColumnField.getCellStyle());
                    sheetInfo.getSheet().setColumnWidth(j, beanColumnField.getColumnWidth());
                }
                rowWriteListener.headerAfterWriteAction(context);
            }
        }

        /**
         * 计算并设置行和列的合并信息
         *
         * @param sheetInfo
         */
        private void calculateMergedRegion(SheetInfo sheetInfo) {
            Sheet sheet = sheetInfo.getSheet();
            List<BeanColumnField> beanColumnFields = sheetInfo.getBeanColumnFields();

            Map<String, List<Integer>> multiValuedMap = new HashMap<>(64);
            for (int i = 0; i < beanColumnFields.size(); i++) {
                BeanColumnField beanColumnField = beanColumnFields.get(i);
                beanColumnField.setColumnIndex(i);

                // 处理行合并
                Set set = new HashSet(Arrays.asList(beanColumnField.getColumnName()));
                if (set.size() != beanColumnField.getColumnName().length) {
                    sheet.addMergedRegion(new CellRangeAddress(beanColumnField.getColumnIndex(),
                            beanColumnField.getColumnName().length - 1, 0, 0));
                    continue;
                }

                // 计算列合并信息
                for (int j = 0; j < beanColumnField.getColumnName().length; j++) {
                    List<Integer> list = multiValuedMap.get(beanColumnField.getColumnName()[j]);
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(beanColumnField.getColumnIndex());
                    multiValuedMap.put(beanColumnField.getColumnName()[j], list);
                }

            }
            // 处理列合并
            for (Map.Entry<String, List<Integer>> stringListEntry : multiValuedMap.entrySet()) {
                List<Integer> list = stringListEntry.getValue();
                if (list.size() > 1) {
                    int firstCel = list.get(0);
                    int lastCel = list.get(list.size() - 1);
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, firstCel, lastCel));
                }
            }

        }

        /**
         * 填充内容信息
         *
         * @param sheetInfo
         */
        private void fillContentInfo(SheetInfo sheetInfo) {
            try {
                List<T> dataList = sheetInfo.getDataList();
                for (int i = 0; i < dataList.size(); i++) {
                    T writeBean = dataList.get(i);
                    Row row = sheetInfo.getSheet().createRow(i + sheetInfo.getHeaderNum());
                    context.setSheet(sheetInfo.getSheet());
                    context.setRow(row);
                    rowWriteListener.contentBeforeWriteAction(writeBean, context);
                    fillRow(sheetInfo.getBeanColumnFields(), row, writeBean);
                    rowWriteListener.contentAfterWriteAction(writeBean, context);
                }
            } catch (Exception e) {
                throw new RuntimeException("fill sheet failed:" + sheetInfo.getSheetName(), e);
            }
        }

        /**
         * 填充内容行信息
         *
         * @param columns
         * @param row
         * @param writeBean
         * @throws Exception
         */
        private void fillRow(List<BeanColumnField> columns, Row row, T writeBean) throws Exception {
            for (int i = 0; i < columns.size(); i++) {
                BeanColumnField beanColumnField = columns.get(i);
                Cell cell = row.createCell(i);
                Field field = beanColumnField.getField();

                if (beanColumnField.isMoneyField()) {
                    // 金额类型特殊处理
                    Number money = (Number) field.get(writeBean);
                    if (beanColumnField.getMoneyUnit() == MoneyUnit.CENT) {
                        money = BigDecimal.valueOf(money.longValue()).divide(BigDecimal.valueOf(100)).doubleValue();
                        CellUtils.fillMoneyCell(cell, money, beanColumnField.getCellStyle());
                    } else {
                        CellUtils.fillMoneyCell(cell, money, beanColumnField.getCellStyle());
                    }
                    continue;
                }

                TripleConsumer tripleConsumer = CellUtils.getHandler(field.getType());
                if (tripleConsumer == null) {
                    throw new RuntimeException("unsupported type:" + field.getType());
                }
                tripleConsumer.accept(cell, field.get(writeBean), beanColumnField.getCellStyle());
            }

        }

        /**
         * 填充尾部行信息
         *
         * @param sheetInfo
         */
        private void fillFooterInfo(SheetInfo sheetInfo) {
            List<FooterRow> footerRowList = sheetInfo.getFooterRowList();
            int sumRows = sheetInfo.getHeaderNum() + sheetInfo.getDataList().size() + sheetInfo.getFooterRowList().size();
            int lastRowIndex = sumRows - 1;
            for (int i = 0; i < footerRowList.size(); i++) {
                int currentRowNum = i + sheetInfo.getHeaderNum() + sheetInfo.getDataList().size();
                Row row = sheetInfo.getSheet().createRow(currentRowNum);
                context.setSheet(sheetInfo.getSheet());
                context.setRow(row);
                rowWriteListener.footerBeforeWriteAction(lastRowIndex - currentRowNum, context);
                List<FooterColumn> footerColumnList = footerRowList.get(i).getFooterColumns();
                int mergeColumns = 0;
                for (int k = 0; k < footerColumnList.size(); k++) {
                    FooterColumn footerColumn = footerColumnList.get(k);
                    Cell cell;
                    if (k == 0) {
                        cell = row.createCell(k);
                    } else {
                        cell = row.createCell(k + mergeColumns);
                    }
                    if (footerColumn.getMergeColumnNum() != 0) {
                        // 列合并信息
                        sheetInfo.getSheet().addMergedRegion(new CellRangeAddress(currentRowNum, currentRowNum,
                                k, footerColumn.getMergeColumnNum()));
                        for (int h = 0; h < footerColumn.getMergeColumnNum(); h++) {
                            Cell emptyCell = row.createCell(k + 1 + h);
                            CellUtils.fillStrCell(emptyCell, footerColumn.getColumnValue(), sheetInfo.getFooterCellStyle());
                        }
                        mergeColumns += footerColumn.getMergeColumnNum();
                    }
                    if (footerColumn.isFunc()) {
                        if (sheetInfo.getDataList().size() == 0) {
                            CellUtils.fillStrCell(cell, "", sheetInfo.getFooterCellStyle());
                        } else {
                            char columnAlphabet = (char) ('@' + cell.getColumnIndex() + 1);
                            int firstDataRowNum = sheetInfo.getHeaderNum() + 1;
                            int lastDataRowNum = sheetInfo.getHeaderNum() + sheetInfo.getDataList().size();
                            String funcStr = String.format("%s(%s:%s)", footerColumn.getColumnValue(),
                                    "" + columnAlphabet + firstDataRowNum, "" + columnAlphabet + lastDataRowNum);
                            CellUtils.fillFuncCell(cell, funcStr, sheetInfo.getFooterCellStyle());
                        }
                    } else {
                        TripleConsumer tripleConsumer = CellUtils.getHandler(footerColumn.getColumnValue().getClass());
                        tripleConsumer.accept(cell, footerColumn.getColumnValue(), sheetInfo.getFooterCellStyle());
                    }
                }
                // 填充剩余的列
                for (int j = footerColumnList.size() + mergeColumns; j < sheetInfo.getBeanColumnFields().size(); j++) {
                    Cell cell = row.createCell(j);
                    CellUtils.fillStrCell(cell, "", sheetInfo.getFooterCellStyle());
                }
                rowWriteListener.footerAfterWriteAction(lastRowIndex - currentRowNum, context);
            }
        }

        private void init(SheetInfo sheetInfo) {
            sheetInfo.setHeaderCellStyle(StyleUtils.getCommonCellStyle(workbook, sheetInfo.getHeaderStyle()));
            sheetInfo.setContentCellStyle(StyleUtils.getCommonCellStyle(workbook, sheetInfo.getContentStyle()));
            sheetInfo.setFooterCellStyle(StyleUtils.getCommonCellStyle(workbook, sheetInfo.getFooterStyle()));

            Field[] fields = sheetInfo.getDataClz().getDeclaredFields();
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
                        beanColumnField.setColumnOrder(excelColumn.columnOrder());
                        beanColumnField.setColumnWidth(excelColumn.columnWidth());

                        ExcelColumnMoney excelColumnMoney = field.getAnnotation(ExcelColumnMoney.class);
                        if (excelColumnMoney != null) {
                            beanColumnField.setMoneyField(true);
                            beanColumnField.setMoneyUnit(excelColumnMoney.moneyUnit());
                            CellStyle style = StyleUtils.getDecimalCellStyle(workbook, excelColumnMoney.moneyFormat());
                            beanColumnField.setCellStyle(style);
                            return beanColumnField;
                        }

                        if (fieldType == Date.class || fieldType == Timestamp.class) {
                            beanColumnField.setDateField(true);
                            ExcelColumnDate excelColumnDate = field.getAnnotation(ExcelColumnDate.class);
                            if (excelColumnDate != null) {
                                CellStyle style = StyleUtils.getDateCellStyle(workbook, excelColumnDate.datePattern());
                                beanColumnField.setCellStyle(style);
                            } else {
                                CellStyle style = StyleUtils.getDateCellStyle(workbook);
                                beanColumnField.setCellStyle(style);
                            }
                        } else if (fieldType == Double.class || fieldType == double.class || fieldType == BigDecimal.class) {
                            beanColumnField.setDecimalField(true);
                            ExcelColumnDecimal excelColumnDecimal = field.getAnnotation(ExcelColumnDecimal.class);
                            if (excelColumnDecimal != null) {
                                CellStyle style = StyleUtils.getDecimalCellStyle(workbook, excelColumnDecimal.decimalFormat());
                                beanColumnField.setCellStyle(style);
                            } else {
                                CellStyle style = StyleUtils.getDecimalCellStyle(workbook);
                                beanColumnField.setCellStyle(style);
                            }
                        } else {
                            beanColumnField.setCellStyle(sheetInfo.getContentCellStyle());
                        }
                        return beanColumnField;
                    })
                    .filter(beanColumnField -> beanColumnField != null)
                    .sorted(Comparator.comparingInt(BeanColumnField::getColumnOrder))
                    .collect(Collectors.toList());
            sheetInfo.setSheet(workbook.createSheet(sheetInfo.getSheetName()));
            sheetInfo.setBeanColumnFields(beanColumnFields);
        }
    }

}
