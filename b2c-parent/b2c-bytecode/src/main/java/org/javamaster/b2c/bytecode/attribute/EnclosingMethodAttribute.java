package org.javamaster.b2c.bytecode.attribute;


import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

import java.io.DataInputStream;

/**
 * 描述一个类为局部类或匿名类时的访问范围(1.5)
 *
 * @author yudong
 * @date 2019/6/26
 */
public class EnclosingMethodAttribute extends AttributeInfo {
    private short classIndex;
    private short methodIndex;

    public EnclosingMethodAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        classIndex = infoStream.readShort();
        methodIndex = infoStream.readShort();
    }

    public Object getClassName() {
        ConstantInfo constantInfo = constantPool.getConstantInfos()[classIndex];
        return constantInfo.getBytesValue();
    }

    public Object getMethodInfo() {
        ConstantInfo constantInfo = constantPool.getConstantInfos()[methodIndex];
        return constantInfo.getBytesValue();
    }

}
