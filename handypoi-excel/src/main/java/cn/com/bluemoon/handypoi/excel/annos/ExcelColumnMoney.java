package cn.com.bluemoon.handypoi.excel.annos;

import cn.com.bluemoon.handypoi.excel.enums.MoneyUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yudong
 * @date 2019/6/9
 */
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumnMoney {

    /**
     * 金额格式
     */
    String moneyFormat();

    /**
     * 金额单位
     */
    MoneyUnit moneyUnit();


}
