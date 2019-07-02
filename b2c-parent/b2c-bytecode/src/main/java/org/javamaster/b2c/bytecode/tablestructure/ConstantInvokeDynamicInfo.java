package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantInvokeDynamicInfo extends ConstantInfo {
    private short bootstrapMethodAttrIndex;
    private short nameAndTypeIndex;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        bootstrapMethodAttrIndex = dataInputStream.readShort();
        nameAndTypeIndex = dataInputStream.readShort();
    }

    @Override
    public Object getBytesValue() {
        // TODO
        return null;
    }

    public short getBootstrapMethodAttrIndex() {
        return bootstrapMethodAttrIndex;
    }

    public short getNameAndTypeIndex() {
        return nameAndTypeIndex;
    }

}
