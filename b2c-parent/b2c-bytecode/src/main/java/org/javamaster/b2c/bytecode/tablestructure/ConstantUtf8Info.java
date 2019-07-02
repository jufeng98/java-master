package org.javamaster.b2c.bytecode.tablestructure;


import org.javamaster.b2c.bytecode.utils.StrUtils;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantUtf8Info extends ConstantInfo {
    /**
     * UTF-8编码的字符串字节长度
     */
    private short length;
    /**
     * 长度为length的UTF-8编码字符串的字节数组
     */
    private byte[] bytes;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        length = dataInputStream.readShort();
        bytes = new byte[length];
        dataInputStream.read(bytes);
    }

    @Override
    public String getBytesValue() {
        return StrUtils.getStringValue(bytes);
    }

    public short getLength() {
        return length;
    }

    public byte[] getBytes() {
        return bytes;
    }

}
