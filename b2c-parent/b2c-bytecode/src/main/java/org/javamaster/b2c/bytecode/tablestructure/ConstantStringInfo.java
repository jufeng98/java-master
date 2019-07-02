package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantStringInfo extends ConstantInfo {
    /**
     *  指向UTF-8类型的常量池索引
     */
    private short index;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        index = dataInputStream.readShort();
    }

    @Override
    public Object getBytesValue() {
        return getStringValue();
    }

    public String getStringValue() {
        ConstantUtf8Info constantUtf8Info = (ConstantUtf8Info) constantPool.getConstantInfos()[index];
        return constantUtf8Info.getBytesValue();
    }

    public short getIndex() {
        return index;
    }

}
