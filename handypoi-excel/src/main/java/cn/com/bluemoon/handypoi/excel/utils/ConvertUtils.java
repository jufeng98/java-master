package cn.com.bluemoon.handypoi.excel.utils;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.Cell;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * @author yudong
 * @date 2019/6/9
 */
public class ConvertUtils {
    public static Object primitiveType(Class<?> fieldTypeClz, Cell cell) {
        Object value = null;
        try {
            value = cell.getNumericCellValue();
        } catch (Exception e) {
            // 非Number列
        }
        if (value == null) {
            if (fieldTypeClz == long.class) {
                value = Long.parseLong(cell.getStringCellValue());
            } else if (fieldTypeClz == double.class
                    || fieldTypeClz == float.class) {
                value = Double.parseDouble(cell.getStringCellValue());
            } else {
                value = Integer.parseInt(cell.getStringCellValue());
            }
        } else {
            Double doubleValue = ((Double) value);
            if (fieldTypeClz == long.class) {
                value = doubleValue.longValue();
            } else if (fieldTypeClz == double.class
                    || fieldTypeClz == float.class) {
                value = doubleValue.doubleValue();
            } else {
                value = doubleValue.intValue();
            }
        }
        Object beanValue = fieldTypeClz.cast(value);
        return beanValue;
    }

    public static Object numberType(Class<?> fieldTypeClz, Cell cell) {
        Object value = null;
        try {
            value = cell.getNumericCellValue();
        } catch (Exception e) {
            // 非Number列
        }
        if (value == null) {
            if (fieldTypeClz == Long.class) {
                value = Long.parseLong(cell.getStringCellValue());
            } else if (fieldTypeClz == Double.class
                    || fieldTypeClz == Float.class) {
                value = Double.parseDouble(cell.getStringCellValue());
            } else {
                value = Integer.parseInt(cell.getStringCellValue());
            }
        } else {
            Double doubleValue = ((Double) value);
            if (fieldTypeClz == Long.class) {
                value = doubleValue.longValue();
            } else if (fieldTypeClz == Double.class
                    || fieldTypeClz == Float.class) {
                value = doubleValue.doubleValue();
            } else {
                value = doubleValue.intValue();
            }
        }
        Object beanValue = fieldTypeClz.cast(value);
        return beanValue;
    }

    public static Object dateType(Class<?> fieldTypeClz, Cell cell, String datePattern) {
        Date date = null;
        try {
            date = cell.getDateCellValue();
        } catch (Exception e) {
            // 非date列
        }
        if (date == null) {
            String dateStr = cell.getStringCellValue();
            if (StringUtils.isNotBlank(dateStr)) {
                try {
                    date = DateUtils.parseDate(dateStr, Locale.SIMPLIFIED_CHINESE, datePattern);
                } catch (ParseException e) {
                    throw new RuntimeException("parse error:" + dateStr, e);
                }
            }
        }
        Object beanValue = fieldTypeClz.cast(date);
        return beanValue;
    }
}
