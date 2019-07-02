package org.javamaster.b2c.bytecode.attribute;

import java.io.DataInputStream;

/**
 * 描述类,方法等被声明废弃
 *
 * @author yudong
 * @date 2019/6/26
 */
public class DeprecatedAttribute extends AttributeInfo {

    public DeprecatedAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) {

    }
}
