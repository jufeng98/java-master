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
    private static Map<Class<?>, TripleConsumer<Cell, Object, CellStyle>> map = new HashMap<>();

    static {
        map.put(byte.class, CellUtils::fillNumberCell);
        map.put(Byte.class, CellUtils::fillNumberCell);

        map.put(short.class, CellUtils::fillNumberCell);
        map.put(Short.class, CellUtils::fillNumberCell);

        map.put(int.class, CellUtils::fillNumberCell);
        map.put(Integer.class, CellUtils::fillNumberCell);

        map.put(long.class, CellUtils::fillNumberCell);
        map.put(Long.class, CellUtils::fillNumberCell);

        map.put(BigInteger.class, CellUtils::fillNumberCell);

        map.put(double.class, CellUtils::fillDecimalCell);
        map.put(Double.class, CellUtils::fillDecimalCell);

        map.put(BigDecimal.class, CellUtils::fillDecimalCell);

        map.put(String.class, CellUtils::fillStrCell);

        map.put(Date.class, CellUtils::fillDateCell);
        map.put(Timestamp.class, CellUtils::fillDateCell);

    }


    public static TripleConsumer getHandler(Class<?> clz) {
        return map.get(clz);
    }

    public static void fillFuncCell(Cell cell, String funcStr, CellStyle style) {
        cell.setCellType(CellType.NUMERIC);
        cell.setCellFormula(funcStr);
        if (funcStr == null) {
            return;
        }
        cell.setCellStyle(style);
    }

    public static void fillNumberCell(Cell cell, Number number, CellStyle style) {
        cell.setCellType(CellType.NUMERIC);
        cell.setCellStyle(style);
        if (number == null) {
            return;
        }
        cell.setCellValue(number.doubleValue());
    }

    public static void fillDecimalCell(Cell cell, Number number, CellStyle style) {
        cell.setCellType(CellType.NUMERIC);
        cell.setCellStyle(style);
        if (number == null) {
            return;
        }
        cell.setCellValue(number.doubleValue());
    }

    public static void fillMoneyCell(Cell cell, Number money, CellStyle style) {
        cell.setCellType(CellType.NUMERIC);
        cell.setCellStyle(style);
        if (money == null) {
            return;
        }
        cell.setCellValue(money.doubleValue());
    }

    public static void fillStrCell(Cell cell, String str, CellStyle style) {
        str = StringUtils.defaultString(str);
        cell.setCellType(CellType.STRING);
        cell.setCellStyle(style);
        cell.setCellValue(str);
    }

    public static void fillDateCell(Cell cell, Date date, CellStyle style) {
        cell.setCellStyle(style);
        cell.setCellType(CellType.NUMERIC);
        if (date == null) {
            return;
        }
        cell.setCellValue(date);
    }


    public static void fillStrCell(Cell cell, Object o, CellStyle style) {
        fillStrCell(cell, (String) o, style);
    }

    public static void fillDecimalCell(Cell cell, Object o, CellStyle style) {
        fillDecimalCell(cell, (Double) o, style);
    }

    public static void fillNumberCell(Cell cell, Object o, CellStyle style) {
        fillNumberCell(cell, (Number) o, style);
    }

    public static void fillDateCell(Cell cell, Object o, CellStyle style) {
        fillDateCell(cell, (Date) o, style);
    }

}
