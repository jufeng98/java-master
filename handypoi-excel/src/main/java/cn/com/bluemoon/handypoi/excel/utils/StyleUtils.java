package cn.com.bluemoon.handypoi.excel.utils;

import cn.com.bluemoon.handypoi.excel.model.Style;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author yudong
 * @date 2019/6/9
 */
public class StyleUtils {

    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DECIMAL_PATTERN = "0.00";

    public static CellStyle getDecimalCellStyle(Workbook workbook) {
        return getDecimalCellStyle(workbook, DEFAULT_DECIMAL_PATTERN);
    }


    public static CellStyle getDecimalCellStyle(Workbook workbook, String format) {
        Style.StyleBuilder styleBuilder = Style.builder();
        styleBuilder.decimalPattern(format);
        return getCommonCellStyle(workbook, styleBuilder.build());
    }

    public static CellStyle getDateCellStyle(Workbook workbook) {
        return getDateCellStyle(workbook, DEFAULT_DATE_PATTERN);
    }

    public static CellStyle getDateCellStyle(Workbook workbook, String pattern) {
        Style.StyleBuilder styleBuilder = Style.builder();
        styleBuilder.datePattern(pattern);
        return getCommonCellStyle(workbook, styleBuilder.build());
    }


    public static CellStyle getCommonCellStyle(Workbook workbook) {
        return getCommonCellStyle(workbook, Style.builder().build());
    }

    public static CellStyle getCommonCellStyle(Workbook workbook, Style style) {
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) style.getFontHeightInPoints());
        font.setFontName(style.getFontName());
        font.setColor((short) style.getFontColor());
        font.setBold(style.getFontBold());
        font.setUnderline((byte) style.getFontUnderline());
        font.setItalic(style.isFontItalic());
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);

        if (style.isNeedBottomBorder()) {
            cellStyle.setBorderBottom(style.getBorderWeight());
            cellStyle.setBottomBorderColor((short) style.getBorderColor());
        }
        if (style.isNeedLeftBorder()) {
            cellStyle.setBorderLeft(style.getBorderWeight());
            cellStyle.setLeftBorderColor((short) style.getBorderColor());
        }
        if (style.isNeedRightBorder()) {
            cellStyle.setBorderRight(style.getBorderWeight());
            cellStyle.setRightBorderColor((short) style.getBorderColor());
        }
        if (style.isNeedTopBorder()) {
            cellStyle.setBorderTop(style.getBorderWeight());
            cellStyle.setTopBorderColor((short) style.getBorderColor());
        }
        cellStyle.setWrapText(style.isAutoLineFeed());
        cellStyle.setAlignment(style.getAlignment());

        cellStyle.setVerticalAlignment(style.getVerticalAlignment());
        if (style.getForegroundColor() != 0) {
            cellStyle.setFillForegroundColor((short) style.getForegroundColor());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (style.getBackgroundColor() != 0) {
            cellStyle.setFillBackgroundColor((short) style.getBackgroundColor());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (style.getDatePattern() != null) {
            DataFormat format = workbook.createDataFormat();
            cellStyle.setDataFormat(format.getFormat(style.getDatePattern()));
        }
        if (style.getDecimalPattern() != null) {
            DataFormat format = workbook.createDataFormat();
            cellStyle.setDataFormat(format.getFormat(style.getDecimalPattern()));
        }
        return cellStyle;
    }

}
