package cn.com.bluemoon.handypoi.excel.example;

import cn.com.bluemoon.handypoi.excel.annos.ExcelColumn;
import cn.com.bluemoon.handypoi.excel.annos.ExcelColumnDecimal;
import cn.com.bluemoon.handypoi.excel.annos.ExcelColumnMoney;
import cn.com.bluemoon.handypoi.excel.enums.MoneyUnit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author yudong
 * @date 2019/7/3
 */
public class ComplexExampleBean {
    @ExcelColumn(columnName = {"公司名占位符", "账单编号:", "序号"}, columnWidth = 3000)
    private int index;
    @ExcelColumn(columnName = {"公司名占位符", "账单编号占位符", "品牌名称"})
    private String brandName;
    @ExcelColumnDecimal(decimalFormat = "0.00%")
    @ExcelColumn(columnName = {"公司名占位符", "结算期数:", "服务费率"})
    private double serviceRate;
    @ExcelColumn(columnName = {"公司名占位符", "结算期数占位符", "衣物数量"})
    private int clothesCount;
    @ExcelColumnMoney(moneyFormat = "0.00", moneyUnit = MoneyUnit.CENT)
    @ExcelColumn(columnName = {"公司名占位符", "日期:", "服务费核算基数(元)"}, columnWidth = 9000)
    private long serviceFeeBase;
    @ExcelColumnMoney(moneyFormat = "0.00", moneyUnit = MoneyUnit.CENT)
    @ExcelColumn(columnName = {"公司名占位符", "日期占位符", "服务费金额(元)"}, columnWidth = 7000)
    private long serviceFee;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public double getServiceRate() {
        return serviceRate;
    }

    public void setServiceRate(double serviceRate) {
        this.serviceRate = serviceRate;
    }

    public int getClothesCount() {
        return clothesCount;
    }

    public void setClothesCount(int clothesCount) {
        this.clothesCount = clothesCount;
    }

    public long getServiceFeeBase() {
        return serviceFeeBase;
    }

    public void setServiceFeeBase(long serviceFeeBase) {
        this.serviceFeeBase = serviceFeeBase;
    }

    public long getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(long serviceFee) {
        this.serviceFee = serviceFee;
    }
}
