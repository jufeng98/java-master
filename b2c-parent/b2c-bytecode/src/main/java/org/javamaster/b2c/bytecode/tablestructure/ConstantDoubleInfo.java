package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantDoubleInfo extends ConstantInfo {
    /**
     * 按照高位在前存储的double值
     */
    private double bytes;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        bytes = dataInputStream.readDouble();
    }

    @Override
    public Object getBytesValue() {
        return getBytes();
    }

    public double getBytes() {
        return bytes;
    }
}
