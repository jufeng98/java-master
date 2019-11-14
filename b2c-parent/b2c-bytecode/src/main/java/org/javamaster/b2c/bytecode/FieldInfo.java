package org.javamaster.b2c.bytecode;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.javamaster.b2c.bytecode.access.FieldAccess;
import org.javamaster.b2c.bytecode.attribute.AttributeInfo;
import org.javamaster.b2c.bytecode.modifier.FieldModifier;
import org.javamaster.b2c.bytecode.tablestructure.ConstantUtf8Info;

import java.io.DataInputStream;
import java.util.List;

/**
 * 描述当前类或接口声明的所有字段，但不包括从父类或父接口继承的部分
 *
 * @author yudong
 * @date 2019/1/11
 */
@JsonPropertyOrder({"accessFlags", "access", "nameIndex", "name", "descriptorIndex", "descriptor"})
public class FieldInfo {
    /**
     * 字段访问标志
     */
    private short accessFlags;
    /**
     * 字段名称常量池索引
     */
    private short nameIndex;
    /**
     * B  byte        有符号字节型数
     * C  char        Unicode 字符，UTF-16 编码
     * D  double      双精度浮点数
     * F  float       单精度浮点数
     * I  int         整型数
     * J  long        长整数
     * S  short       有符号短整数
     * Z  boolean     布尔值 true/false
     * L  Classname   reference 一个名为<Classname>的实例
     * [  reference   一个一维数组
     */
    private short descriptorIndex;
    /**
     * 属性数组长度
     */
    private short attributeCount;
    /**
     * 属性数组
     */
    private AttributeInfo[] attributeInfos;

    @JsonIgnore
    private ConstantPool constantPool;

    void initFieldInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws Exception {
        this.constantPool = constantPool;
        accessFlags = dataInputStream.readShort();
        nameIndex = dataInputStream.readShort();
        descriptorIndex = dataInputStream.readShort();
        attributeCount = dataInputStream.readShort();
        attributeInfos = new AttributeInfo[attributeCount];
        for (int i = 0; i < attributeInfos.length; i++) {
            attributeInfos[i] = AttributeInfo.read(dataInputStream, constantPool);
        }
    }

    public List<FieldModifier> getAccess() {
        FieldAccess fieldAccess = new FieldAccess(accessFlags);
        return fieldAccess.getModifiers();
    }

    public String getName() {
        ConstantUtf8Info constantInfo = ((ConstantUtf8Info) constantPool.getConstantInfos()[nameIndex]);
        return constantInfo.getBytesValue();
    }

    public String getDescriptor() {
        ConstantUtf8Info constantInfo = ((ConstantUtf8Info) constantPool.getConstantInfos()[descriptorIndex]);
        return constantInfo.getBytesValue();
    }

    public short getAccessFlags() {
        return accessFlags;
    }

    public void setAccessFlags(short accessFlags) {
        this.accessFlags = accessFlags;
    }

    public short getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(short nameIndex) {
        this.nameIndex = nameIndex;
    }

    public short getDescriptorIndex() {
        return descriptorIndex;
    }

    public void setDescriptorIndex(short descriptorIndex) {
        this.descriptorIndex = descriptorIndex;
    }

    public short getAttributeCount() {
        return attributeCount;
    }

    public void setAttributeCount(short attributeCount) {
        this.attributeCount = attributeCount;
    }

    public AttributeInfo[] getAttributeInfos() {
        return attributeInfos;
    }

    public void setAttributeInfos(AttributeInfo[] attributeInfos) {
        this.attributeInfos = attributeInfos;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }
}
