package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantMethodHandleInfo extends ConstantInfo {
    private byte referenceKind;
    private short referenceIndex;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        referenceKind = dataInputStream.readByte();
        referenceIndex = dataInputStream.readShort();
    }

    @Override
    public Object getBytesValue() {
        // TODO
        return null;
    }

    public byte getReferenceKind() {
        return referenceKind;
    }

    public short getReferenceIndex() {
        return referenceIndex;
    }
}
