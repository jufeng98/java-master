package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2019/7/8
 */

@AllOpen
class Column {
    var name: String? = null

    /**
     * 对应的java.sql.Types的SQL类型(列类型ID)
     */
    var type = 0

    /**
     * java.sql.Types类型名称(列类型名称)
     */
    var typeName: String? = null
    var size = 0
    var digits = 0

    /**
     * //（通常是10或2） --未知
     */
    var precRadix = 0

    /**
     * 0 (columnNoNulls) - 该列不允许为空
     * 1 (columnNullable) - 该列允许为空
     * 2 (columnNullableUnknown) - 不确定该列是否为空
     */
    var nullable = 0
    var remarks: String? = null

    /**
     * 默认值
     */
    var def: String? = null

    /**
     * 对于 char 类型，该长度是列中的最大字节数
     */
    var charOctetLength = 0

    /**
     * 表中列的索引（从1开始）
     */
    var ordinalPosition = 0

    /**
     * ISO规则用来确定某一列的是否可为空(等同于NULLABLE的值:[ 0:'YES'; 1:'NO'; 2:''; ])
     * YES -- 该列可以有空值;
     * NO -- 该列不能为空;
     * 空字符串--- 不知道该列是否可为空
     */
    var isNullable: String? = null

    /**
     * 指示此列是否是自动递增
     * YES -- 该列是自动递增的
     * NO -- 该列不是自动递增
     * 空字串--- 不能确定该列是否自动递增
     */
    var isAutoincrement: String? = null

    var primaryKey = false

    override fun toString(): String {
        return String.format("%s  %s(%s)  %s", name, typeName, size, remarks)
    }
}
