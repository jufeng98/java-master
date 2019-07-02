package org.javamaster.b2c.bytecode;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.javamaster.b2c.bytecode.attribute.AttributeInfo;
import org.javamaster.b2c.bytecode.modifier.MethodModifier;
import org.javamaster.b2c.bytecode.tablestructure.ConstantUtf8Info;

import java.io.DataInputStream;
import java.util.List;

/**
 * 只描述当前类或接口中声明的方法，不包括从父类或父接口继承的方法
 *
 * @author yudong
 * @date 2019/1/11
 */
@JsonPropertyOrder({"accessFlags", "access", "nameIndex", "name", "descriptorIndex", "descriptor"})
public class MethodInfo {
    /**
     * 方法访问标志
     */
    private short accessFlags;
    /**
     * 方法名称常量池索引
     */
    private short nameIndex;
    /**
     * 方法描述符
     * 例如方法: Object invoke(int i, double d, Thread t)
     * 其描述符为:(IDLjava/lang/Thread;)Ljava/lang/Object;
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

    public void initMethodInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws Exception {
        accessFlags = dataInputStream.readShort();
        nameIndex = dataInputStream.readShort();
        descriptorIndex = dataInputStream.readShort();
        this.constantPool = constantPool;
        attributeCount = dataInputStream.readShort();
        attributeInfos = new AttributeInfo[attributeCount];
        for (int i = 0; i < attributeInfos.length; i++) {
            attributeInfos[i] = AttributeInfo.read(dataInputStream, constantPool);
        }
    }

    public List<MethodModifier> getAccess() {
        return MethodModifier.getModifiers(accessFlags);
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

    public short getNameIndex() {
        return nameIndex;
    }

    public short getDescriptorIndex() {
        return descriptorIndex;
    }

    public short getAttributeCount() {
        return attributeCount;
    }

    public AttributeInfo[] getAttributeInfos() {
        return attributeInfos;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

}
