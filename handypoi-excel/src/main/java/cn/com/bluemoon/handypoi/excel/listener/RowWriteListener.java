package cn.com.bluemoon.handypoi.excel.listener;

import cn.com.bluemoon.handypoi.excel.resolve.ExcelContext;

/**
 * @author yudong
 * @date 2019/6/9
 */
public interface RowWriteListener {

    /**
     * 标题行写入前回调方法
     *
     * @param rowIndex 行下标
     * @param context
     */
    default void headerBeforeWriteAction(int rowIndex, ExcelContext context) {

    }

    /**
     * 标题行写入后回调方法
     *
     * @param rowIndex 行下标
     * @param context
     */
    default void headerAfterWriteAction(int rowIndex, ExcelContext context) {

    }

    /**
     * 内容行写入前回调方法
     *
     * @param data     待写入的data
     * @param rowIndex 行下标
     * @param context
     */
    default void contentBeforeWriteAction(Object data, int rowIndex, ExcelContext context) {

    }

    /**
     * 内容行写入前回调方法
     *
     * @param data     待写入的data
     * @param rowIndex 行下标
     * @param context
     */
    default void contentAfterWriteAction(Object data, int rowIndex, ExcelContext context) {

    }
}
