package org.javamaster.b2c.bytecode.attribute;

/**
 * 作用同RuntimeVisibleAnnotationsAttribute,不过作用对象为方法参数(1.5)
 *
 * @author yudong
 * @date 2019/6/26
 */
public class RuntimeVisibleParameterAnnotationsAttribute extends ParameterAnnotationsAttribute {

    public RuntimeVisibleParameterAnnotationsAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }
}
