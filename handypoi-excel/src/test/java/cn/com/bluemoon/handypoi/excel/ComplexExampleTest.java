package cn.com.bluemoon.handypoi.excel;

import cn.com.bluemoon.handypoi.excel.enums.ExcelType;
import cn.com.bluemoon.handypoi.excel.example.ComplexExampleBean;
import cn.com.bluemoon.handypoi.excel.listener.RowWriteListener;
import cn.com.bluemoon.handypoi.excel.model.FooterColumn;
import cn.com.bluemoon.handypoi.excel.model.FooterRow;
import cn.com.bluemoon.handypoi.excel.model.Style;
import cn.com.bluemoon.handypoi.excel.resolve.*;
import cn.com.bluemoon.handypoi.excel.utils.StyleUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author yudong
 * @date 2019/6/9
 */
public class ComplexExampleTest {

    @Test
    public void testWrite() throws Exception {

        List<ComplexExampleBean> list = IntStream.rangeClosed(1, 6).mapToObj(this::generateBean).collect(Collectors.toList());

        ExcelWriter<ComplexExampleBean> excelWriter = new ExcelWriter<>(ExcelType.XLS);
        SheetInfo<ComplexExampleBean> sheetInfo = new SheetInfo<>(list, ComplexExampleBean.class, "结算信息", 3);

        sheetInfo.setFooterRowList(getFooterInfos());

        excelWriter.addSheetInfo(sheetInfo);
        excelWriter.setRowWriteListener(new RowWriteListener<ComplexExampleBean>() {
            @Override
            public void headerAfterWriteAction(ExcelContext context) {
                int rowIndex = context.getRow().getRowNum();
                if (rowIndex == 0) {
                    context.getSheet().setDisplayGridlines(false);
                    context.getRow().setHeight((short) 800);
                    Style style = Style.builder()
                            .fontName("宋体")
                            .fontHeightInPoints(14)
                            .fontBold(true)
                            .needLeftBorder(false)
                            .needTopBorder(false)
                            .needRightBorder(false)
                            .borderColor(IndexedColors.GREY_50_PERCENT.index)
                            .build();
                    CellStyle cellStyle = StyleUtils.getCommonCellStyle(context.getWorkbook(), style);
                    for (int i = 0; i < context.getRow().getLastCellNum(); i++) {
                        context.getRow().getCell(i).setCellStyle(cellStyle);
                        context.getRow().getCell(i).setCellValue("XXXXX公司");
                    }
                } else if (rowIndex == 1) {
                    context.getRow().setHeight((short) 800);
                    Style.StyleBuilder styleBuilder = Style.builder()
                            .needLeftBorder(false)
                            .needTopBorder(false)
                            .needRightBorder(false)
                            .borderColor(IndexedColors.GREY_50_PERCENT.index);
                    for (int i = 0; i < context.getRow().getLastCellNum(); i++) {
                        if (i == context.getRow().getLastCellNum() - 1) {
                            styleBuilder.needRightBorder(true);
                        }
                        if (i % 2 == 0) {
                            styleBuilder.alignment(HorizontalAlignment.RIGHT);
                        } else {
                            styleBuilder.alignment(HorizontalAlignment.LEFT);
                        }
                        CellStyle cellStyle = StyleUtils.getCommonCellStyle(context.getWorkbook(), styleBuilder.build());
                        context.getRow().getCell(i).setCellStyle(cellStyle);
                    }
                    context.getRow().getCell(1).setCellValue("T0000000123");
                    context.getRow().getCell(3).setCellValue("10期");
                    context.getRow().getCell(5).setCellValue(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                } else {
                    context.getRow().setHeight((short) 400);
                }
            }

            @Override
            public void contentAfterWriteAction(ComplexExampleBean data, ExcelContext context) {
                context.getRow().setHeight((short) 400);
            }

            @Override
            public void footerAfterWriteAction(int reverseRowIndex, ExcelContext context) {
                Style.StyleBuilder styleBuilder = Style.builder()
                        .needLeftBorder(false)
                        .needTopBorder(false)
                        .needRightBorder(false)
                        .needBottomBorder(false);
                if (reverseRowIndex == 5) {
                    context.getRow().setHeight((short) 400);
                    Style style = Style.builder()
                            .decimalPattern("0.00")
                            .build();
                    CellStyle cellStyle = StyleUtils.getCommonCellStyle(context.getWorkbook(), style);
                    context.getRow().getCell(4).setCellStyle(cellStyle);
                    context.getRow().getCell(5).setCellStyle(cellStyle);
                } else if (reverseRowIndex == 4) {
                    for (int i = 0; i < context.getRow().getLastCellNum(); i++) {
                        CellStyle cellStyle = StyleUtils.getCommonCellStyle(context.getWorkbook(), styleBuilder.build());
                        context.getRow().getCell(i).setCellStyle(cellStyle);
                    }
                } else if (reverseRowIndex == 3) {
                    styleBuilder
                            .fontName("宋体")
                            .fontHeightInPoints(12)
                            .fontBold(true)
                            .alignment(HorizontalAlignment.LEFT);
                    for (int i = 0; i < context.getRow().getLastCellNum(); i++) {
                        CellStyle cellStyle = StyleUtils.getCommonCellStyle(context.getWorkbook(), styleBuilder.build());
                        context.getRow().getCell(i).setCellStyle(cellStyle);
                    }
                } else {
                    styleBuilder
                            .fontName("DengXian")
                            .fontHeightInPoints(12)
                            .alignment(HorizontalAlignment.LEFT);
                    for (int i = 0; i < context.getRow().getLastCellNum(); i++) {
                        CellStyle cellStyle = StyleUtils.getCommonCellStyle(context.getWorkbook(), styleBuilder.build());
                        context.getRow().getCell(i).setCellStyle(cellStyle);
                    }
                }
            }
        });
        ExcelWriterService excelWriterService = excelWriter.addSheetFinish();

        Workbook workbook = excelWriterService.getWorkBook();
        File file = new File("D:\\complex-excel-test.xls");
        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();

    }

    @Test
    public void testRead() throws Exception {

        File file = new File("D:\\complex-excel-test.xls");
        InputStream inputStream = new FileInputStream(file);
        ExcelReader<ComplexExampleBean> excelReader = new ExcelReader<>(ExcelType.XLS, inputStream, ComplexExampleBean.class, 3, 6);
        excelReader.setRowReadListener((bean, context) -> {
            System.out.println(bean);
            return true;
        });
        excelReader.read();
        List<ComplexExampleBean> list = excelReader.getResultList();
        System.out.println(list);

    }

    private ComplexExampleBean generateBean(int i) {
        ComplexExampleBean bean = new ComplexExampleBean();
        bean.setIndex(i);
        bean.setBrandName("brand name" + i);
        bean.setServiceRate(0.105 * i);
        bean.setClothesCount(RandomUtils.nextInt(2, 12));
        bean.setServiceFeeBase(1000000L * i);
        bean.setServiceFee(100000L * i);
        return bean;
    }

    private List<FooterRow> getFooterInfos() {
        List<FooterRow> footerRowList = new ArrayList<>(6);

        List<FooterColumn> footerColumnList1 = new ArrayList<>(6);
        footerColumnList1.add(new FooterColumn("合计", 2));
        footerColumnList1.add(new FooterColumn("SUM", true));
        footerColumnList1.add(new FooterColumn("SUM", true));
        footerColumnList1.add(new FooterColumn("SUM", true));
        FooterRow footerRow1 = new FooterRow(footerColumnList1);
        footerRowList.add(footerRow1);

        List<FooterColumn> footerColumnList2 = new ArrayList<>(6);
        footerColumnList2.add(new FooterColumn("", 5));
        FooterRow footerRow2 = new FooterRow(footerColumnList2);
        footerRowList.add(footerRow2);

        List<FooterColumn> footerColumnList3 = new ArrayList<>(6);
        footerColumnList3.add(new FooterColumn("收款单位账户信息", 5));
        FooterRow footerRow3 = new FooterRow(footerColumnList3);
        footerRowList.add(footerRow3);

        List<FooterColumn> footerColumnList4 = new ArrayList<>(6);
        footerColumnList4.add(new FooterColumn("银行账户名称：XXX", 5));
        FooterRow footerRow4 = new FooterRow(footerColumnList4);
        footerRowList.add(footerRow4);

        List<FooterColumn> footerColumnList5 = new ArrayList<>(6);
        footerColumnList5.add(new FooterColumn("开户行：中国XX银行", 5));
        FooterRow footerRow5 = new FooterRow(footerColumnList5);
        footerRowList.add(footerRow5);

        List<FooterColumn> footerColumnList6 = new ArrayList<>(6);
        footerColumnList6.add(new FooterColumn("账号：62222 02360 02485 XXXXX", 5));
        FooterRow footerRow6 = new FooterRow(footerColumnList6);
        footerRowList.add(footerRow6);
        return footerRowList;
    }
}
