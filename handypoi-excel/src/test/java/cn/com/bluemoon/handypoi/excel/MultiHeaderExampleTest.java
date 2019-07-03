package cn.com.bluemoon.handypoi.excel;

import cn.com.bluemoon.handypoi.excel.enums.ExcelType;
import cn.com.bluemoon.handypoi.excel.example.MultiHeadExampleBean;
import cn.com.bluemoon.handypoi.excel.listener.RowWriteListener;
import cn.com.bluemoon.handypoi.excel.model.FooterColumn;
import cn.com.bluemoon.handypoi.excel.model.FooterRow;
import cn.com.bluemoon.handypoi.excel.model.Style;
import cn.com.bluemoon.handypoi.excel.resolve.ExcelContext;
import cn.com.bluemoon.handypoi.excel.resolve.ExcelReader;
import cn.com.bluemoon.handypoi.excel.resolve.ExcelWriter;
import cn.com.bluemoon.handypoi.excel.resolve.ExcelWriterService;
import cn.com.bluemoon.handypoi.excel.resolve.SheetInfo;
import cn.com.bluemoon.handypoi.excel.utils.StyleUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author yudong
 * @date 2019/6/9
 */
public class MultiHeaderExampleTest {

    @Test
    public void testWrite() throws Exception {

        List<MultiHeadExampleBean> list = IntStream.rangeClosed(1, 3).mapToObj(i -> generateBean(i)).collect(Collectors.toList());

        ExcelWriter excelWriter = new ExcelWriter(ExcelType.XLS);
        SheetInfo sheetInfo = new SheetInfo(list, MultiHeadExampleBean.class, "交易信息", 2);

        List<FooterColumn> footerColumnList = new ArrayList<>(1);
        footerColumnList.add(new FooterColumn("合计", 1));
        footerColumnList.add(new FooterColumn("SUM", true));
        footerColumnList.add(new FooterColumn("SUM", true));
        FooterRow footerRow = new FooterRow(footerColumnList);

        sheetInfo.setFooterRowList(Arrays.asList(footerRow));

        excelWriter.addSheetInfo(sheetInfo);
        excelWriter.setRowWriteListener(new RowWriteListener<MultiHeadExampleBean>() {
            @Override
            public void headerAfterWriteAction(ExcelContext context) {
                int rowIndex = context.getRow().getRowNum();
                if (rowIndex == 1) {
                    Style style = Style.builder().foregroundColor(IndexedColors.BLUE_GREY.index).build();
                    CellStyle cellStyle = StyleUtils.getCommonCellStyle(context.getWorkbook(), style);
                    context.getRow().getCell(2).setCellStyle(cellStyle);
                }
            }

            @Override
            public void contentAfterWriteAction(MultiHeadExampleBean data, ExcelContext context) {
                System.out.println(context.getRow().getRowNum());
            }

            @Override
            public void footerAfterWriteAction(int reverseRowIndex, ExcelContext context) {
                if (reverseRowIndex == 0) {
                    Style style = Style.builder().foregroundColor(IndexedColors.RED.index).decimalPattern("0.00").build();
                    CellStyle cellStyle = StyleUtils.getCommonCellStyle(context.getWorkbook(), style);
                    context.getRow().getCell(2).setCellStyle(cellStyle);
                    Style style1 = Style.builder().foregroundColor(IndexedColors.GREEN.index).decimalPattern("0.0000").build();
                    CellStyle cellStyle1 = StyleUtils.getCommonCellStyle(context.getWorkbook(), style1);
                    context.getRow().getCell(3).setCellStyle(cellStyle1);
                }
            }
        });
        ExcelWriterService excelWriterService = excelWriter.addSheetFinish();

        Workbook workbook = excelWriterService.getWorkBook();
        File file = new File("D:\\multi-header-excel-test.xls");
        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();

    }

    @Test
    public void testRead() throws Exception {

        File file = new File("D:\\multi-header-excel-test.xls");
        InputStream inputStream = new FileInputStream(file);
        ExcelReader excelReader = new ExcelReader(ExcelType.XLS, inputStream, MultiHeadExampleBean.class, 2, 1);
        excelReader.setRowReadListener((bean, context) -> {
            System.out.println(bean);
            return true;
        });
        excelReader.read();
        List<MultiHeadExampleBean> list = excelReader.getResultList();
        System.out.println(list);

    }

    private MultiHeadExampleBean generateBean(int i) {
        MultiHeadExampleBean bean = new MultiHeadExampleBean();
        bean.setIndex(i);
        bean.setOrderCode("TC000000" + i);
        bean.setOrderPrice(i * 1000L);
        bean.setOrderBase(i * 1000.2555);
        bean.setPayTime(DateUtils.addMonths(new Date(), -i * 2));
        bean.setCustomerName("yudong" + i + 2);
        bean.setCustomerPhone("13800138000" + i);
        bean.setProvince("Guangdong" + i);
        bean.setCity("Guangzhou" + i);
        bean.setVillage("Tianhe cun" + i);
        bean.setStreet("jichang road" + i);
        return bean;
    }
}
