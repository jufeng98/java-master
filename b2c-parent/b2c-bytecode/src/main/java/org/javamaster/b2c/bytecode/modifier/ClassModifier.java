package org.javamaster.b2c.bytecode.modifier;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 如果一个 Class 文件被设置了 ACC_INTERFACE 标志，那么同时也得设置ACC_ABSTRACT 标志
 * 如果设置了 ACC_ANNOTATION 标记，ACC_INTERFACE 也必须被同时设置
 * <p>
 * ACC_SUPER 标记是为了向后兼容旧编译器编译的 Class 文件而存在的，目前 Java 虚拟机
 * 的编译器都应当设置这个标志
 * <p>
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public enum ClassModifier {
    /**
     * 可以被包的类外访问。
     */
    ACC_PUBLIC((short) 0x0001),
    /**
     * 仅用于内部类
     */
    ACC_PRIVATE((short) 0x0002),
    /**
     * 只能被子类访问及包内类访问。
     */
    ACC_PROTECTED((short) 0x0004),
    /**
     * 仅用于内部类
     */
    ACC_STATIC((short) 0x0008),
    /**
     * 不允许有子类。
     */
    ACC_FINAL((short) 0x0010),
    /**
     * 当用到 invokespecial 指令时，需要特殊处理的父类方法。
     */
    ACC_SUPER((short) 0x0020),
    /**
     * 标识定义的是接口而不是类。
     */
    ACC_INTERFACE((short) 0x0200),
    /**
     * 不能被实例化。
     */
    ACC_ABSTRACT((short) 0x0400),
    /**
     * 标识并非 Java 源码生成的代码即由编译器自己产生的
     */
    ACC_SYNTHETIC((short) 0x1000),
    /**
     * 标识注解类型
     */
    ACC_ANNOTATION((short) 0x2000),
    /**
     * 标识枚举类型
     */
    ACC_ENUM((short) 0x4000);

    private short value;

    ClassModifier(short value) {
        this.value = value;
    }

    public static List<ClassModifier> getModifiers(short accessFlags) {
        List<ClassModifier> classModifiers = Lists.newArrayList();
        for (ClassModifier modifier : ClassModifier.values()) {
            if ((accessFlags & modifier.value) != 0) {
                classModifiers.add(modifier);
            }
        }
        return classModifiers;
    }
}
