package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantMethodRefInfo extends ConstantInfo {
    /**
     * 类或接口名称常量池索引
     */
    private short classOrInterfaceIndex;
    /**
     * 指向常量池NameAndType类型常量的索引
     */
    private short nameAndTypeIndex;

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) throws Exception {
        classOrInterfaceIndex = dataInputStream.readShort();
        nameAndTypeIndex = dataInputStream.readShort();
    }

    @Override
    public Object getBytesValue() {
        return getClassOfInterfaceName() + ":" + getNameAndTypeIndexName();
    }

    public String getClassOfInterfaceName() {
        ConstantClassInfo constantInfo = (ConstantClassInfo) constantPool.getConstantInfos()[classOrInterfaceIndex];
        ConstantUtf8Info constantUtf8Info = (ConstantUtf8Info) constantPool.getConstantInfos()[constantInfo.getIndex()];
        return constantUtf8Info.getBytesValue();
    }

    public String getNameAndTypeIndexName() {
        ConstantNameAndTypeInfo constantInfo = (ConstantNameAndTypeInfo) constantPool.getConstantInfos()[nameAndTypeIndex];
        ConstantUtf8Info constantUtf8Info = (ConstantUtf8Info) constantPool.getConstantInfos()[constantInfo.getFieldOrMethodNameIndex()];
        ConstantUtf8Info constantUtf8Info1 = (ConstantUtf8Info) constantPool.getConstantInfos()[constantInfo.getFieldOrMethodDescIndex()];
        return constantUtf8Info.getBytesValue() + ":" + constantUtf8Info1.getBytesValue();
    }


    public short getClassOrInterfaceIndex() {
        return classOrInterfaceIndex;
    }

    public short getNameAndTypeIndex() {
        return nameAndTypeIndex;
    }

}
