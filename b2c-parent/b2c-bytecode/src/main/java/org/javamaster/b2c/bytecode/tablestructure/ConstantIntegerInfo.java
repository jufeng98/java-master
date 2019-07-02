package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantIntegerInfo extends ConstantInfo {
    /**
     * 按照高位在前存储的int值
     */
    private int bytes;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        bytes = dataInputStream.readInt();
    }

    @Override
    public Object getBytesValue() {
        return getBytes();
    }

    public int getBytes() {
        return bytes;
    }

}
