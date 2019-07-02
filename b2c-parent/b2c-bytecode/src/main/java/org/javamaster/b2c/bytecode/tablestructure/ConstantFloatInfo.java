package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantFloatInfo extends ConstantInfo {
    /**
     * 按照高位在前存储的 float值
     */
    private float bytes;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        bytes = dataInputStream.readFloat();
    }

    @Override
    public Object getBytesValue() {
        return getBytes();
    }

    public float getBytes() {
        return bytes;
    }

}
