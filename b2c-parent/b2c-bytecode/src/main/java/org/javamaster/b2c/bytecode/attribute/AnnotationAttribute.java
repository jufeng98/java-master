package org.javamaster.b2c.bytecode.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.javamaster.b2c.bytecode.ConstantPool;
import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

import java.io.DataInputStream;

/**
 * @author yudong
 * @date 2019/6/26
 */
@JsonPropertyOrder({"attributeKeyIndex", "attributeKey", "annotationAttributeValue"})
public class AnnotationAttribute {
    /**
     * 注解属性索引下标
     */
    private short attributeKeyIndex;

    private AnnotationAttributeValue annotationAttributeValue;

    @JsonIgnore
    private ConstantPool constantPool;

    public void initAttribute(DataInputStream infoStream, ConstantPool constantPool) throws Exception {
        attributeKeyIndex = infoStream.readShort();
        annotationAttributeValue = AnnotationAttributeValue.read(infoStream);
        this.constantPool = constantPool;
    }

    public Object getAttributeKey() {
        ConstantInfo constantInfo = constantPool.getConstantInfos()[attributeKeyIndex];
        return constantInfo.getBytesValue();
    }

    public short getAttributeKeyIndex() {
        return attributeKeyIndex;
    }

    public AnnotationAttributeValue getAnnotationAttributeValue() {
        return annotationAttributeValue;
    }

}

