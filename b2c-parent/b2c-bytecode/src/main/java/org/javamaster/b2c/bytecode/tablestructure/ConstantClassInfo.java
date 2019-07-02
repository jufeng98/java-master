package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantClassInfo extends ConstantInfo {
    /**
     * 类或接口名称常量池索引
     */
    private short index;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        index = dataInputStream.readShort();
    }

    @Override
    public Object getBytesValue() {
        return getClassValue();
    }

    public String getClassValue() {
        ConstantUtf8Info constantInfo = (ConstantUtf8Info) constantPool.getConstantInfos()[index];
        return constantInfo.getBytesValue();
    }

    public short getIndex() {
        return index;
    }

}
