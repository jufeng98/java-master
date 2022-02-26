package cn.com.bluemoon.handypoi.excel.utils;

import cn.com.bluemoon.handypoi.excel.function.TripleConsumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yudong
 * @date 2019/6/9
 */
public class CellUtils {
    private static final Map<Class<?>, TripleConsumer<Cell, Object, CellStyle>> MAP = new HashMap<>();

    static {
        MAP.put(byte.class, CellUtils::fillNumberCell);
        MAP.put(Byte.class, CellUtils::fillNumberCell);

        MAP.put(short.class, CellUtils::fillNumberCell);
        MAP.put(Short.class, CellUtils::fillNumberCell);

        MAP.put(int.class, CellUtils::fillNumberCell);
        MAP.put(Integer.class, CellUtils::fillNumberCell);

        MAP.put(long.class, CellUtils::fillNumberCell);
        MAP.put(Long.class, CellUtils::fillNumberCell);

        MAP.put(BigInteger.class, CellUtils::fillNumberCell);

        MAP.put(double.class, CellUtils::fillDecimalCell);
        MAP.put(Double.class, CellUtils::fillDecimalCell);

        MAP.put(BigDecimal.class, CellUtils::fillDecimalCell);

        MAP.put(String.class, CellUtils::fillStrCell);

        MAP.put(Date.class, CellUtils::fillDateCell);
        MAP.put(Timestamp.class, CellUtils::fillDateCell);

    }


    public static TripleConsumer<Cell, Object, CellStyle> getHandler(Class<?> clz) {
        return MAP.get(clz);
    }

    public static void fillFuncCell(Cell cell, String funcStr, CellStyle style) {
        cell.setCellStyle(style);
        if (funcStr == null) {
            return;
        }
        cell.setCellFormula(funcStr);
        cell.setCellType(CellType.NUMERIC);
    }

    private static void fillNumberCell(Cell cell, Number number, CellStyle style) {
        cell.setCellStyle(style);
        if (number == null) {
            return;
        }
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(number.doubleValue());
    }

    private static void fillDecimalCell(Cell cell, Number number, CellStyle style) {
        cell.setCellStyle(style);
        if (number == null) {
            return;
        }
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(number.doubleValue());
    }

    public static void fillMoneyCell(Cell cell, Number money, CellStyle style) {
        cell.setCellStyle(style);
        if (money == null) {
            return;
        }
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(money.doubleValue());
    }

    public static void fillStrCell(Cell cell, String str, CellStyle style) {
        str = StringUtils.defaultString(str);
        cell.setCellType(CellType.STRING);
        cell.setCellStyle(style);
        cell.setCellValue(str);
    }

    private static void fillDateCell(Cell cell, Date date, CellStyle style) {
        cell.setCellStyle(style);
        if (date == null) {
            return;
        }
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(date);
    }


    public static void fillStrCell(Cell cell, Object o, CellStyle style) {
        fillStrCell(cell, (String) o, style);
    }

    private static void fillDecimalCell(Cell cell, Object o, CellStyle style) {
        fillDecimalCell(cell, (Double) o, style);
    }

    private static void fillNumberCell(Cell cell, Object o, CellStyle style) {
        fillNumberCell(cell, (Number) o, style);
    }

    private static void fillDateCell(Cell cell, Object o, CellStyle style) {
        fillDateCell(cell, (Date) o, style);
    }

}
