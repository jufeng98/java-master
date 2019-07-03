package cn.com.bluemoon.handypoi.excel.utils;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.Cell;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * @author yudong
 * @date 2019/6/9
 */
public class ConvertUtils {
    public static double primitiveType(Class<?> fieldTypeClz, Cell cell) {
        double value = Double.NaN;
        try {
            value = cell.getNumericCellValue();
        } catch (Exception e) {
            // 非Number列
        }
        if (value != Double.NaN) {
            return value;
        }
        // 取值失败,则把cell值当成String类型来取
        String numberStr = cell.getStringCellValue();
        if (StringUtils.isBlank(numberStr)) {
            return 0.00;
        }
        if (fieldTypeClz == long.class) {
            return Long.parseLong(numberStr);
        } else if (fieldTypeClz == double.class) {
            return Double.parseDouble(numberStr);
        } else if (fieldTypeClz == float.class) {
            return Float.parseFloat(numberStr);
        } else {
            return Integer.parseInt(numberStr);
        }
    }

    public static Number numberType(Class<?> fieldTypeClz, Cell cell) {
        Number value = null;
        try {
            value = cell.getNumericCellValue();
        } catch (Exception e) {
            // 非Number类型的列
        }
        if (value != null) {
            if (fieldTypeClz == BigDecimal.class) {
                return BigDecimal.valueOf(value.doubleValue());
            } else if (fieldTypeClz == BigInteger.class) {
                return BigInteger.valueOf(value.longValue());
            } else if (fieldTypeClz == Long.class) {
                return Long.valueOf(value.longValue());
            } else if (fieldTypeClz == Double.class) {
                return Double.valueOf(value.doubleValue());
            } else if (fieldTypeClz == Float.class) {
                return Float.valueOf(value.floatValue());
            } else {
                return Integer.valueOf(value.intValue());
            }
        }
        // 取值失败,则把cell值当成String类型来取
        String numberStr = cell.getStringCellValue();
        if (StringUtils.isBlank(numberStr)) {
            return null;
        }
        if (fieldTypeClz == BigDecimal.class) {
            return new BigDecimal(numberStr);
        } else if (fieldTypeClz == BigInteger.class) {
            return new BigInteger(numberStr);
        } else if (fieldTypeClz == Long.class) {
            return Long.valueOf(numberStr);
        } else if (fieldTypeClz == Double.class) {
            return Double.valueOf(numberStr);
        } else if (fieldTypeClz == Float.class) {
            return Float.valueOf(numberStr);
        } else {
            return Integer.valueOf(numberStr);
        }
    }

    public static Date dateType(Class<?> fieldTypeClz, Cell cell, String datePattern) {
        Date date = tryGetDate(cell, datePattern);
        if (date == null) {
            return null;
        }
        if (fieldTypeClz == Timestamp.class) {
            return new Timestamp(date.getTime());
        } else {
            return date;
        }
    }

    private static Date tryGetDate(Cell cell, String datePattern) {
        Date date = null;
        try {
            date = cell.getDateCellValue();
        } catch (Exception e) {
            // 非date类型的列
        }
        if (date != null) {
            return date;
        }
        // 取值失败,则把cell值当成String类型来取
        String dateStr = cell.getStringCellValue();
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return DateUtils.parseDate(dateStr, Locale.SIMPLIFIED_CHINESE, datePattern);
        } catch (ParseException e) {
            throw new RuntimeException("parse error:" + dateStr, e);
        }
    }
}
