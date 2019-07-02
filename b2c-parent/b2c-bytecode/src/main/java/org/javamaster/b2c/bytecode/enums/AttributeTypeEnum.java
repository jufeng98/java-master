package org.javamaster.b2c.bytecode.enums;

import com.google.common.collect.Maps;
import org.javamaster.b2c.bytecode.attribute.AnnotationDefaultAttribute;
import org.javamaster.b2c.bytecode.attribute.AttributeInfo;
import org.javamaster.b2c.bytecode.attribute.CodeAttribute;
import org.javamaster.b2c.bytecode.attribute.ConstantValueAttribute;
import org.javamaster.b2c.bytecode.attribute.DeprecatedAttribute;
import org.javamaster.b2c.bytecode.attribute.EnclosingMethodAttribute;
import org.javamaster.b2c.bytecode.attribute.ExceptionsAttribute;
import org.javamaster.b2c.bytecode.attribute.InnerClassesAttribute;
import org.javamaster.b2c.bytecode.attribute.LineNumberTableAttribute;
import org.javamaster.b2c.bytecode.attribute.LocalVariableTableAttribute;
import org.javamaster.b2c.bytecode.attribute.LocalVariableTypeTableAttribute;
import org.javamaster.b2c.bytecode.attribute.RuntimeInvisibleAnnotationsAttribute;
import org.javamaster.b2c.bytecode.attribute.RuntimeInvisibleParameterAnnotationsAttribute;
import org.javamaster.b2c.bytecode.attribute.RuntimeVisibleAnnotationsAttribute;
import org.javamaster.b2c.bytecode.attribute.RuntimeVisibleParameterAnnotationsAttribute;
import org.javamaster.b2c.bytecode.attribute.SignatureAttribute;
import org.javamaster.b2c.bytecode.attribute.SourceDebugExtensionAttribute;
import org.javamaster.b2c.bytecode.attribute.SourceFileAttribute;
import org.javamaster.b2c.bytecode.attribute.StackMapTableAttribute;
import org.javamaster.b2c.bytecode.attribute.SyntheticAttribute;

import java.util.Map;

/**
 * @author yudong
 * @date 2019/6/26
 */
public enum AttributeTypeEnum {
    CODE("Code", CodeAttribute.class),
    EXCEPTIONS("Exceptions", ExceptionsAttribute.class),
    LINE_NUMBER_TABLE("LineNumberTable", LineNumberTableAttribute.class),
    LOCAL_VARIABLE_TABLE("LocalVariableTable", LocalVariableTableAttribute.class),
    LOCAL_VARIABLE_TYPE_TABLE("LocalVariableTypeTable", LocalVariableTypeTableAttribute.class),
    SOURCE_FILE("SourceFile", SourceFileAttribute.class),
    CONSTANT_VALUE("ConstantValue", ConstantValueAttribute.class),
    INNER_CLASSES("InnerClasses", InnerClassesAttribute.class),
    DEPRECATED("Deprecated", DeprecatedAttribute.class),
    SYNTHETIC("Synthetic", SyntheticAttribute.class),
    STACK_MAP_TABLE("StackMapTable", StackMapTableAttribute.class),
    ENCLOSING_METHOD("EnclosingMethod", EnclosingMethodAttribute.class),
    SIGNATURE("Signature", SignatureAttribute.class),
    SOURCE_DEBUG_EXTENSION("SourceDebugExtension", SourceDebugExtensionAttribute.class),
    RUNTIME_VISIBLE_ANNOTATIONS("RuntimeVisibleAnnotations", RuntimeVisibleAnnotationsAttribute.class),
    RUNTIME_INVISIBLE_ANNOTATIONS("RuntimeInvisibleAnnotations", RuntimeInvisibleAnnotationsAttribute.class),
    RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS("RuntimeVisibleParameterAnnotations", RuntimeVisibleParameterAnnotationsAttribute.class),
    RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS("RuntimeInvisibleParameterAnnotations", RuntimeInvisibleParameterAnnotationsAttribute.class),
    ANNOTATION_DEFAULT("AnnotationDefault", AnnotationDefaultAttribute.class),
    ;
    private String attributeName;
    private Class<? extends AttributeInfo> attributeTypeClz;
    private static Map<String, Class<? extends AttributeInfo>> map = Maps.newHashMap();

    static {
        for (AttributeTypeEnum value : AttributeTypeEnum.values()) {
            map.put(value.attributeName, value.attributeTypeClz);
        }
    }

    AttributeTypeEnum(String attributeName, Class<? extends AttributeInfo> attributeTypeClz) {
        this.attributeName = attributeName;
        this.attributeTypeClz = attributeTypeClz;
    }

    public static Class<? extends AttributeInfo> getAttributeType(String attributeName) {
        return map.getOrDefault(attributeName, AttributeInfo.class);
    }
}
