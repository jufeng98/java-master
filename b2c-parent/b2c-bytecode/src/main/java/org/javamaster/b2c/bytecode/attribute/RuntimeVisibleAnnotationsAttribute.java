package org.javamaster.b2c.bytecode.attribute;

/**
 * 描述哪些注解是运行时可见的(1.5)
 *
 * @author yudong
 * @date 2019/6/26
 */
public class RuntimeVisibleAnnotationsAttribute extends AnnotationsAttribute {

    public RuntimeVisibleAnnotationsAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }
}
