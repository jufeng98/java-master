package org.javamaster.b2c.bytecode;


import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

/**
 * 常量池，constant_pool 是一种表结构，它包含 Class 文件结构及其子结构中引用的
 * 所有字符串常量、 类或接口名、字段名和其它常量。 常量池中的每一项都具备相同的
 * 格式特征，第一个字节作为类型标记用于识别该项是哪种类型的常量，称为tag byte 。
 * 常量池的索引范围是 1 至 constant_pool_count−1，0被JVM保留
 * <p>
 *
 * @author yudong
 * @date 2019/1/11
 */
public class ConstantPool {
    private final ConstantInfo[] constantInfos;

    ConstantPool(int count) {
        constantInfos = new ConstantInfo[count];
    }

    public ConstantInfo[] getConstantInfos() {
        return constantInfos;
    }

}
