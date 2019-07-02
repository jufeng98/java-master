package org.javamaster.b2c.bytecode.attribute;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.javamaster.b2c.bytecode.ConstantPool;
import org.javamaster.b2c.bytecode.enums.AttributeTypeEnum;
import org.javamaster.b2c.bytecode.jackson.ByteArraySerializer;
import org.javamaster.b2c.bytecode.tablestructure.ConstantUtf8Info;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * @author yudong
 * @date 2019/1/11
 */
@JsonPropertyOrder({"attributeNameIndex", "attributeName", "attributeLength", "info"})
public abstract class AttributeInfo {
    /**
     * 属性名称常量池索引
     */
    protected short attributeNameIndex;
    /**
     * 属性数组长度,即info数组长度
     */
    protected int attributeLength;
    /**
     * 属性数组
     */
    @JsonSerialize(using = ByteArraySerializer.class)
    protected byte[] info;

    @JsonIgnore
    protected ConstantPool constantPool;

    public AttributeInfo(short attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    public static AttributeInfo read(DataInputStream dataInputStream, ConstantPool constantPool) throws Exception {
        short attrNameIndex = dataInputStream.readShort();
        String name = ((ConstantUtf8Info) constantPool.getConstantInfos()[attrNameIndex]).getBytesValue();
        Class<? extends AttributeInfo> attributeTypeClz = AttributeTypeEnum.getAttributeType(name);
        AttributeInfo attributeInfo = attributeTypeClz.getDeclaredConstructor(short.class).newInstance(attrNameIndex);
        attributeInfo.constantPool = constantPool;
        attributeInfo.initAttributeInfo(dataInputStream, constantPool);
        return attributeInfo;
    }

    public void initAttributeInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws Exception {
        attributeLength = dataInputStream.readInt();
        info = new byte[attributeLength];
        this.constantPool = constantPool;
        if (attributeLength > 0) {
            dataInputStream.readFully(info);
        }
        if (attributeLength == 0) {
            return;
        }
        try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(info))) {
            initSubInfo(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化info数组代表的意义
     *
     * @param infoStream
     * @throws Exception
     */
    public abstract void initSubInfo(DataInputStream infoStream) throws Exception;


    public String getAttributeName() {
        ConstantUtf8Info constantInfo = ((ConstantUtf8Info) constantPool.getConstantInfos()[attributeNameIndex]);
        return constantInfo.getBytesValue();
    }

    public short getAttributeNameIndex() {
        return attributeNameIndex;
    }

    public int getAttributeLength() {
        return attributeLength;
    }

    public byte[] getInfo() {
        return info;
    }

}
