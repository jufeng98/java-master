package cn.com.bluemoon.handypoi.excel.listener;

import cn.com.bluemoon.handypoi.excel.resolve.ExcelContext;

/**
 * @author yudong
 * @date 2019/6/9
 */
@FunctionalInterface
public interface RowReadListener<T> {

    /**
     * 行读取回调方法
     *
     * @param bean    行信息填充的对象
     * @param context
     * @return 返回false将会过滤掉此填充的对象
     */
    boolean accept(T bean, ExcelContext context);

}
