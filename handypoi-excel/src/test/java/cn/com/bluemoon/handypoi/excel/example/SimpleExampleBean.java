package cn.com.bluemoon.handypoi.excel.example;

import cn.com.bluemoon.handypoi.excel.annos.ExcelColumn;
import cn.com.bluemoon.handypoi.excel.annos.ExcelColumnMoney;
import cn.com.bluemoon.handypoi.excel.enums.MoneyUnit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author yudong
 * @date 2019/6/9
 */
public class SimpleExampleBean {

    @ExcelColumn(columnName = "序号")
    private Integer index;

    @ExcelColumn(columnName = "订单编码")
    private String orderCode;

    @ExcelColumnMoney(moneyFormat = "0.00", moneyUnit = MoneyUnit.CENT)
    @ExcelColumn(columnName = "订单金额")
    private Long orderPrice;

    @ExcelColumn(columnName = "订单基数")
    private Double orderBase;

    @ExcelColumn(columnName = "支付时间", columnWidth = 9000)
    private Date payTime;

    @ExcelColumn(columnName = "确认时间", columnWidth = 9000)
    private Timestamp confirmTime;

    @ExcelColumn(columnName = "消费者姓名")
    private String customerName;

    @ExcelColumn(columnName = "消费者联系电话")
    private String customerPhone;

    @ExcelColumn(columnName = "省份")
    private String province;

    @ExcelColumn(columnName = "城市")
    private String city;

    @ExcelColumn(columnName = "县区")
    private String village;

    @ExcelColumn(columnName = "街道")
    private String street;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Long getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(Long orderPrice) {
        this.orderPrice = orderPrice;
    }

    public Double getOrderBase() {
        return orderBase;
    }

    public void setOrderBase(Double orderBase) {
        this.orderBase = orderBase;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public Timestamp getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(Timestamp confirmTime) {
        this.confirmTime = confirmTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}

