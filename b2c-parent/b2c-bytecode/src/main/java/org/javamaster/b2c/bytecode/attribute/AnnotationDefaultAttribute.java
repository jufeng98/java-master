package org.javamaster.b2c.bytecode.attribute;

import java.io.DataInputStream;

/**
 * 描述注解的默认值(1.5)
 * 注解类才有这个属性
 *
 * @author yudong
 * @date 2019/6/26
 */
public class AnnotationDefaultAttribute extends AttributeInfo {

    private AnnotationAttributeValue annotationAttributeValue;

    public AnnotationDefaultAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        annotationAttributeValue = AnnotationAttributeValue.read(infoStream);
    }

    public AnnotationAttributeValue getAnnotationAttributeValue() {
        return annotationAttributeValue;
    }

}
