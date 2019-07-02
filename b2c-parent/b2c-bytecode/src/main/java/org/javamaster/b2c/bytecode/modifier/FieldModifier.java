package org.javamaster.b2c.bytecode.modifier;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public enum FieldModifier {
    /**
     * public，表示字段可以从任何包访问。
     */
    ACC_PUBLIC((short) 0x0001),
    /**
     * private，表示字段仅能该类自身调用。
     */
    ACC_PRIVATE((short) 0x0002),
    /**
     * protected，表示字段可以被子类调用。
     */
    ACC_PROTECTED((short) 0x0004),
    /**
     * static，表示静态字段。
     */
    ACC_STATIC((short) 0x0008),
    /**
     * final，表示字段定义后值无法修改
     */
    ACC_FINAL((short) 0x0010),
    /**
     * synchronized，表示字段是同步的。
     */
    ACC_SYNCHRONIZED((short) 0x0020),
    /**
     * volatile，表示字段是易变的。
     */
    ACC_VOLATILE((short) 0x0040),
    /**
     * transient，表示字段不会被序列化
     */
    ACC_TRANSIENT((short) 0x0080),
    /**
     * enum，表示字段为枚举类型。
     */
    ACC_ENUM((short) 0x0400),
    /**
     * 表示字段由编译器自动产生。
     */
    ACC_SYNTHETIC((short) 0x1000);

    private final short value;

    FieldModifier(short value) {
        this.value = value;
    }

    /**
     * @param accessFlags
     * @return
     */
    public static List<FieldModifier> getModifiers(short accessFlags) {
        List<FieldModifier> fieldModifiers = Lists.newArrayList();
        for (FieldModifier modifier : FieldModifier.values()) {
            if ((accessFlags & modifier.value) != 0) {
                fieldModifiers.add(modifier);
            }
        }
        return fieldModifiers;
    }
}
