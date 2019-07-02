package org.javamaster.b2c.bytecode.attribute;


import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

import java.io.DataInputStream;

/**
 * 描述生成这个class文件的源码名称
 *
 * @author yudong
 * @date 2019/6/26
 */
public class SourceFileAttribute extends AttributeInfo {
    private short sourceFileIndex;

    public SourceFileAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        sourceFileIndex = infoStream.readShort();
    }

    public Object getSourceFileName() {
        ConstantInfo constantInfo = constantPool.getConstantInfos()[sourceFileIndex];
        return constantInfo.getBytesValue();
    }

    public short getSourceFileIndex() {
        return sourceFileIndex;
    }

}
