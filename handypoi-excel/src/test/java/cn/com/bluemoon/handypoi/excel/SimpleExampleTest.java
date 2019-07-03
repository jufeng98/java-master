package cn.com.bluemoon.handypoi.excel;

import cn.com.bluemoon.handypoi.excel.enums.ExcelType;
import cn.com.bluemoon.handypoi.excel.example.SimpleExampleBean;
import cn.com.bluemoon.handypoi.excel.listener.RowWriteListener;
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
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author yudong
 * @date 2019/6/9
 */
public class SimpleExampleTest {

    @Test
    public void testWrite() throws Exception {

        List<SimpleExampleBean> list = IntStream.rangeClosed(1, 3).mapToObj(i -> generateBean(i)).collect(Collectors.toList());

        ExcelWriter excelWriter = new ExcelWriter(ExcelType.XLS);
        SheetInfo sheetInfo = new SheetInfo(list, SimpleExampleBean.class, "交易信息", 1);
        excelWriter.addSheetInfo(sheetInfo);
        excelWriter.setRowWriteListener(new RowWriteListener() {
            @Override
            public void headerAfterWriteAction(ExcelContext context) {
                int rowIndex = context.getRow().getRowNum();
                if (rowIndex == 0) {
                    Style style = Style.builder().foregroundColor(IndexedColors.BLUE_GREY.index).build();
                    CellStyle cellStyle = StyleUtils.getCommonCellStyle(context.getWorkbook(), style);
                    context.getRow().getCell(2).setCellStyle(cellStyle);
                }
            }

            @Override
            public void contentAfterWriteAction(Object data, ExcelContext context) {
                System.out.println(context.getRow().getRowNum());
            }
        });
        ExcelWriterService excelWriterService = excelWriter.addSheetFinish();

        Workbook workbook = excelWriterService.getWorkBook();
        File file = new File("D:\\simple-excel-test.xls");
        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();

    }

    @Test
    public void testRead() throws Exception {

        File file = new File("D:\\simple-excel-test.xls");
        InputStream inputStream = new FileInputStream(file);
        ExcelReader excelReader = new ExcelReader(ExcelType.XLS, inputStream, SimpleExampleBean.class, 1, 0);
        excelReader.setRowReadListener((bean, context) -> {
            System.out.println(bean);
            return true;
        });
        excelReader.read();
        List<SimpleExampleBean> list = excelReader.getResultList();
        System.out.println(list);

    }

    private SimpleExampleBean generateBean(int i) {
        SimpleExampleBean bean = new SimpleExampleBean();
        bean.setIndex(i);
        bean.setOrderCode("KC000000" + i);
        bean.setOrderPrice(i * 1000L);
        bean.setOrderBase(i * 1000.2555);
        bean.setPayTime(DateUtils.addMonths(new Date(), -i * 2));
        bean.setConfirmTime(new Timestamp(DateUtils.addMonths(new Date(), -i * 2).getTime()));
        bean.setCustomerName("yudong" + i * 2);
        bean.setCustomerPhone("13800138000" + i);
        bean.setProvince("Guangdong" + i);
        bean.setCity("Guangzhou" + i);
        bean.setVillage("Tianhe cun" + i);
        bean.setStreet("jichang road" + i);
        return bean;
    }
}
