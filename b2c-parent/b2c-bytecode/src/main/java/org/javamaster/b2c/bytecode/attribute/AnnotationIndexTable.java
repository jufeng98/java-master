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
@JsonPropertyOrder({"annotationInfoIndex", "annotationInfo"})
public class AnnotationIndexTable {
    /**
     * 注解名称常量池索引
     */
    private short annotationInfoIndex;
    /**
     * 属性数组长度
     */
    private short numberOfAnnotationAttributes;
    /**
     * 注解的属性键值对
     */
    private AnnotationAttribute[] annotationAttributes;

    @JsonIgnore
    private ConstantPool constantPool;

    public void initAttribute(DataInputStream infoStream, ConstantPool constantPool) throws Exception {
        this.constantPool = constantPool;
        annotationInfoIndex = infoStream.readShort();
        numberOfAnnotationAttributes = infoStream.readShort();
        if (numberOfAnnotationAttributes == 0) {
            return;
        }
        annotationAttributes = new AnnotationAttribute[numberOfAnnotationAttributes];
        for (int i = 0; i < annotationAttributes.length; i++) {
            AnnotationAttribute annotationAttribute = new AnnotationAttribute();
            annotationAttribute.initAttribute(infoStream, constantPool);
            annotationAttributes[i] = annotationAttribute;
        }
    }

    public Object getAnnotationInfo() {
        ConstantInfo constantInfo = constantPool.getConstantInfos()[annotationInfoIndex];
        return constantInfo.getBytesValue();
    }

    public short getAnnotationInfoIndex() {
        return annotationInfoIndex;
    }

    public short getNumberOfAnnotationAttributes() {
        return numberOfAnnotationAttributes;
    }

    public AnnotationAttribute[] getAnnotationAttributes() {
        return annotationAttributes;
    }
}
