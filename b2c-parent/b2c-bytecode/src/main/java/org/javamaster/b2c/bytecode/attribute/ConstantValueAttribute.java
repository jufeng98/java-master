package org.javamaster.b2c.bytecode.attribute;


import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

import java.io.DataInputStream;

/**
 * 描述虚拟机自动为静态变量赋值,只有被static修饰的静态变量才可以使用此属性,其中属性值仅限于基本类型和String,
 * attributeLength的长度固定为2
 *
 * @author yudong
 * @date 2019/6/26
 */
public class ConstantValueAttribute extends AttributeInfo {
    private short constantValueIndex;

    public ConstantValueAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        constantValueIndex = infoStream.readShort();
    }

    public short getConstantValueIndex() {
        return constantValueIndex;
    }

    public Object getConstantValue() {
        ConstantInfo constantInfo = constantPool.getConstantInfos()[constantValueIndex];
        return constantInfo.getBytesValue();
    }
}
