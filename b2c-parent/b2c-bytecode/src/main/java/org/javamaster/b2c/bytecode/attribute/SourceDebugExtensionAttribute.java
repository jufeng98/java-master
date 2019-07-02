package org.javamaster.b2c.bytecode.attribute;

import java.io.DataInputStream;

/**
 * 描述额外的调试信息,例如jsp文件的调试(1.6)
 *
 * @author yudong
 * @date 2019/6/26
 */
public class SourceDebugExtensionAttribute extends AttributeInfo {

    public SourceDebugExtensionAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) {

    }
}
