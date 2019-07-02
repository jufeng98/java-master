package org.javamaster.b2c.bytecode.attribute;

import java.io.DataInputStream;

/**
 * 描述类,字段或方法是由编译器自行产生的
 *
 * @author yudong
 * @date 2019/6/26
 */
public class SyntheticAttribute extends AttributeInfo {

    public SyntheticAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) {

    }
}
