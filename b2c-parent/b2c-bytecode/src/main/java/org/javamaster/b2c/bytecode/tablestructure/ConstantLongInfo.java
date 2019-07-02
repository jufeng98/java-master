package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantLongInfo extends ConstantInfo {
    /**
     * 按照高位在前存储的long值
     */
    private long bytes;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        bytes = dataInputStream.readLong();
    }

    @Override
    public Object getBytesValue() {
        return getBytes();
    }

    public long getBytes() {
        return bytes;
    }

}
