package org.javamaster.b2c.bytecode.attribute;

import java.io.DataInputStream;

/**
 * 描述类型校验时需要用到的相关信息,加快class文件的校验速度(1.6)
 *
 * @author yudong
 * @date 2019/6/26
 */
public class StackMapTableAttribute extends AttributeInfo {
    private short numberOfEntrys;
    private byte[][] stackMapFrames;

    public StackMapTableAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        numberOfEntrys = infoStream.readShort();
        stackMapFrames = new byte[numberOfEntrys][];
        for (int i = 0; i < stackMapFrames.length; i++) {
            byte[] bytes = new byte[4];
            for (int j = 0; j < bytes.length; j++) {
                bytes[j] = infoStream.readByte();
            }
            stackMapFrames[i] = bytes;
        }
    }

    public short getNumberOfEntrys() {
        return numberOfEntrys;
    }

    public byte[][] getStackMapFrames() {
        return stackMapFrames;
    }
}
