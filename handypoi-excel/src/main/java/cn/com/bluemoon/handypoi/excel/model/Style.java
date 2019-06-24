package cn.com.bluemoon.handypoi.excel.model;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

/**
 * 样式,可调用builder()方法逐步构建此对象
 *
 * @author yudong
 * @date 2019/6/9
 */

public class Style {

    private String fontName;
    private int fontColor;
    private boolean fontBold;
    private int fontUnderline;
    private boolean fontItalic;
    private int fontHeightInPoints;

    private int foregroundColor;
    private int backgroundColor;

    private boolean needLeftBorder;
    private boolean needRightBorder;
    private boolean needTopBorder;
    private boolean needBottomBorder;
    private int borderColor;
    private BorderStyle borderWeight;

    private boolean autoLineFeed;

    private HorizontalAlignment alignment;
    private VerticalAlignment verticalAlignment;

    private String datePattern;
    private String decimalPattern;

    Style(String fontName, int fontColor, boolean fontBold, int fontUnderline, boolean fontItalic, int fontHeightInPoints,
          int foregroundColor, int backgroundColor, boolean needLeftBorder, boolean needRightBorder, boolean needTopBorder,
          boolean needBottomBorder, int borderColor, BorderStyle borderWeight, boolean autoLineFeed, HorizontalAlignment alignment,
          VerticalAlignment verticalAlignment, String datePattern, String decimalPattern) {
        this.fontName = fontName;
        this.fontColor = fontColor;
        this.fontBold = fontBold;
        this.fontUnderline = fontUnderline;
        this.fontItalic = fontItalic;
        this.fontHeightInPoints = fontHeightInPoints;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.needLeftBorder = needLeftBorder;
        this.needRightBorder = needRightBorder;
        this.needTopBorder = needTopBorder;
        this.needBottomBorder = needBottomBorder;
        this.borderColor = borderColor;
        this.borderWeight = borderWeight;
        this.autoLineFeed = autoLineFeed;
        this.alignment = alignment;
        this.verticalAlignment = verticalAlignment;
        this.datePattern = datePattern;
        this.decimalPattern = decimalPattern;
    }

    public static Style.StyleBuilder builder() {
        return new Style.StyleBuilder();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * 大部分属性都赋予了默认值
     */
    public static class StyleBuilder {
        private String fontName = "DengXian";
        private int fontColor = Font.COLOR_NORMAL;
        private boolean fontBold = false;
        private int fontUnderline = Font.U_NONE;
        private boolean fontItalic = false;
        private int fontHeightInPoints = 12;
        /**
         * @see org.apache.poi.ss.usermodel.IndexedColors
         */
        private int foregroundColor;
        /**
         * @see org.apache.poi.ss.usermodel.IndexedColors
         */
        private int backgroundColor;
        private boolean needLeftBorder = true;
        private boolean needRightBorder = true;
        private boolean needTopBorder = true;
        private boolean needBottomBorder = true;
        private int borderColor = HSSFColor.HSSFColorPredefined.GREY_50_PERCENT.getIndex();
        private BorderStyle borderWeight = BorderStyle.THIN;
        private boolean autoLineFeed = false;
        private HorizontalAlignment alignment = HorizontalAlignment.CENTER;
        private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
        private String datePattern;
        private String decimalPattern;

        StyleBuilder() {
        }

        public Style build() {
            return new Style(this.fontName, this.fontColor, this.fontBold, this.fontUnderline, this.fontItalic,
                    this.fontHeightInPoints, this.foregroundColor, this.backgroundColor, this.needLeftBorder,
                    this.needRightBorder, this.needTopBorder, this.needBottomBorder, this.borderColor, this.borderWeight,
                    this.autoLineFeed, this.alignment, this.verticalAlignment, this.datePattern, this.decimalPattern);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        public Style.StyleBuilder fontName(String fontName) {
            this.fontName = fontName;
            return this;
        }

        public Style.StyleBuilder fontColor(int fontColor) {
            this.fontColor = fontColor;
            return this;
        }

        public Style.StyleBuilder fontBold(boolean fontBold) {
            this.fontBold = fontBold;
            return this;
        }

        public Style.StyleBuilder fontUnderline(int fontUnderline) {
            this.fontUnderline = fontUnderline;
            return this;
        }

        public Style.StyleBuilder fontItalic(boolean fontItalic) {
            this.fontItalic = fontItalic;
            return this;
        }

        public Style.StyleBuilder fontHeightInPoints(int fontHeightInPoints) {
            this.fontHeightInPoints = fontHeightInPoints;
            return this;
        }

        public Style.StyleBuilder foregroundColor(int foregroundColor) {
            this.foregroundColor = foregroundColor;
            return this;
        }

        public Style.StyleBuilder backgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Style.StyleBuilder needLeftBorder(boolean needLeftBorder) {
            this.needLeftBorder = needLeftBorder;
            return this;
        }

        public Style.StyleBuilder needRightBorder(boolean needRightBorder) {
            this.needRightBorder = needRightBorder;
            return this;
        }

        public Style.StyleBuilder needTopBorder(boolean needTopBorder) {
            this.needTopBorder = needTopBorder;
            return this;
        }

        public Style.StyleBuilder needBottomBorder(boolean needBottomBorder) {
            this.needBottomBorder = needBottomBorder;
            return this;
        }

        public Style.StyleBuilder borderColor(int borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        public Style.StyleBuilder borderWeight(BorderStyle borderWeight) {
            this.borderWeight = borderWeight;
            return this;
        }

        public Style.StyleBuilder autoLineFeed(boolean autoLineFeed) {
            this.autoLineFeed = autoLineFeed;
            return this;
        }

        public Style.StyleBuilder alignment(HorizontalAlignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public Style.StyleBuilder verticalAlignment(VerticalAlignment verticalAlignment) {
            this.verticalAlignment = verticalAlignment;
            return this;
        }

        public Style.StyleBuilder datePattern(String datePattern) {
            this.datePattern = datePattern;
            return this;
        }

        public Style.StyleBuilder decimalPattern(String decimalPattern) {
            this.decimalPattern = decimalPattern;
            return this;
        }
    }

    public String getFontName() {
        return fontName;
    }

    public int getFontColor() {
        return fontColor;
    }

    public boolean getFontBold() {
        return fontBold;
    }

    public int getFontUnderline() {
        return fontUnderline;
    }

    public boolean isFontItalic() {
        return fontItalic;
    }

    public int getFontHeightInPoints() {
        return fontHeightInPoints;
    }

    public int getForegroundColor() {
        return foregroundColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isNeedLeftBorder() {
        return needLeftBorder;
    }

    public boolean isNeedRightBorder() {
        return needRightBorder;
    }

    public boolean isNeedTopBorder() {
        return needTopBorder;
    }

    public boolean isNeedBottomBorder() {
        return needBottomBorder;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public BorderStyle getBorderWeight() {
        return borderWeight;
    }

    public boolean isAutoLineFeed() {
        return autoLineFeed;
    }

    public HorizontalAlignment getAlignment() {
        return alignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public String getDecimalPattern() {
        return decimalPattern;
    }

}
