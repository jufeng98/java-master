package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantNameAndTypeInfo extends ConstantInfo {
    /**
     * 字段或方法名称常量池索引
     */
    private short fieldOrMethodNameIndex;
    /**
     * 字段或方法描述符常量池索引
     */
    private short fieldOrMethodDescIndex;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        fieldOrMethodNameIndex = dataInputStream.readShort();
        fieldOrMethodDescIndex = dataInputStream.readShort();
    }

    @Override
    public Object getBytesValue() {
        return getFieldOrMethodName() + ":" + getFieldOrMethodDesc();
    }


    public String getFieldOrMethodName() {
        ConstantUtf8Info constantUtf8Info = (ConstantUtf8Info) constantPool.getConstantInfos()[fieldOrMethodNameIndex];
        return constantUtf8Info.getBytesValue();
    }

    public String getFieldOrMethodDesc() {
        ConstantUtf8Info constantUtf8Info = (ConstantUtf8Info) constantPool.getConstantInfos()[fieldOrMethodDescIndex];
        return constantUtf8Info.getBytesValue();
    }

    public short getFieldOrMethodNameIndex() {
        return fieldOrMethodNameIndex;
    }

    public short getFieldOrMethodDescIndex() {
        return fieldOrMethodDescIndex;
    }

}
