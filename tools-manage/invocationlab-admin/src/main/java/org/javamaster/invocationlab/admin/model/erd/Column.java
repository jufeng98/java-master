package org.javamaster.invocationlab.admin.model.erd;

import lombok.Data;

/**
 * @author yudong
 * @date 2019/7/8
 */
@Data
public class Column {
    private String name;
    /**
     * 对应的java.sql.Types的SQL类型(列类型ID)
     */
    private int type;
    /**
     * java.sql.Types类型名称(列类型名称)
     */
    private String typeName;
    private int size;
    private int digits;
    /**
     * //（通常是10或2） --未知
     */
    private int precRadix;
    /**
     * 0 (columnNoNulls) - 该列不允许为空
     * 1 (columnNullable) - 该列允许为空
     * 2 (columnNullableUnknown) - 不确定该列是否为空
     */
    private int nullable;
    private String remarks;
    /**
     * 默认值
     */
    private String def;
    /**
     * 对于 char 类型，该长度是列中的最大字节数
     */
    private int charOctetLength;
    /**
     * 表中列的索引（从1开始）
     */
    private int ordinalPosition;
    /**
     * ISO规则用来确定某一列的是否可为空(等同于NULLABLE的值:[ 0:'YES'; 1:'NO'; 2:''; ])
     * YES -- 该列可以有空值;
     * NO -- 该列不能为空;
     * 空字符串--- 不知道该列是否可为空
     */
    private String isNullable;
    /**
     * 指示此列是否是自动递增
     * YES -- 该列是自动递增的
     * NO -- 该列不是自动递增
     * 空字串--- 不能确定该列是否自动递增
     */
    private String isAutoincrement;

    private boolean primaryKey;

    @Override
    public String toString() {
        return String.format("%s  %s(%s)  %s", name, typeName, size, remarks);
    }
}
