package cn.com.bluemoon.handypoi.excel.annos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注bean属性对应的excel的列信息
 *
 * @author yudong
 * @date 2019/6/9
 */
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {

    /**
     * 列名,传入多个表明是多级表头
     */
    String[] columnName();

    /**
     * 列顺序,越小越靠前
     */
    int columnOrder() default 999;

    /**
     * 列宽
     */
    int columnWidth() default 5000;

}
