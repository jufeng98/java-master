package org.javamaster.b2c.bytecode.modifier;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public enum MethodModifier {
    /**
     * public，方法可以从包外访问
     */
    ACC_PUBLIC((short) 0x0001),
    /**
     * private，方法只能本类中访问
     */
    ACC_PRIVATE((short) 0x0002),
    /**
     * protected，方法在自身和子类可以访问
     */
    ACC_PROTECTED((short) 0x0004),
    /**
     * static，静态方法
     */
    ACC_STATIC((short) 0x0008),
    /**
     * final，方法不能被重写（覆盖）
     */
    ACC_FINAL((short) 0x0010),
    /**
     * synchronized，方法由管程同步
     */
    ACC_SYNCHRONIZED((short) 0x0020),
    /**
     * bridge，方法由编译器产生
     */
    ACC_BRIDGE((short) 0x0040),
    /**
     * 表示方法带有变长参数
     */
    ACC_VARARGS((short) 0x0080),
    /**
     * native，方法引用非 java 语言的本地方法
     */
    ACC_NATIVE((short) 0x0100),
    /**
     * abstract，方法没有具体实现
     */
    ACC_ABSTRACT((short) 0x0400),
    /**
     * strictfp，方法使用 FP-strict 浮点格式
     */
    ACC_STRICTFP((short) 0x0800),
    /**
     * 方法在源文件中不出现，由编译器产生
     */
    ACC_SYNTHETIC((short) 0x1000);

    private final short value;

    MethodModifier(short value) {
        this.value = value;
    }

    /**
     * @param accessFlags
     * @return
     */
    public static List<MethodModifier> getModifiers(short accessFlags) {
        List<MethodModifier> methodModifiers = Lists.newArrayList();
        for (MethodModifier modifier : MethodModifier.values()) {
            if ((accessFlags & modifier.value) != 0) {
                methodModifiers.add(modifier);
            }
        }
        return methodModifiers;
    }
}
