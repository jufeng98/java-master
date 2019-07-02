package org.javamaster.b2c.bytecode;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import org.javamaster.b2c.bytecode.attribute.AttributeInfo;
import org.javamaster.b2c.bytecode.jackson.HexSerializer;
import org.javamaster.b2c.bytecode.modifier.ClassModifier;
import org.javamaster.b2c.bytecode.tablestructure.ConstantDoubleInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantLongInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantPaddingInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 解析class文件
 *
 * @author yudong
 * @date 2019/1/11
 */
@JsonPropertyOrder({"magic", "minorVersion", "majorVersion", "constantPoolCount", "constantPool", "accessFlags",
        "access", "thisClass", "thisClassName", "superClass", "superClassName", "interfaceCount", "interfaces", "interfacesName"})
public class ClassFileReader {
    /**
     * 魔数，魔数的唯一作用是确定这个文件是否为一个能被虚拟机所接受的 Class 文件。魔
     * 数值固定为 0xCAFEBABE，不会改变
     */
    @JsonSerialize(using = HexSerializer.class)
    private int magic;
    /**
     * 副版本号
     */
    private short minorVersion;
    /**
     * 主版本号,例如52表示JDK1.8
     */
    private short majorVersion;
    /**
     * 常量池数量
     */
    private short constantPoolCount;

    private ConstantPool constantPool;
    /**
     * 类访问标志
     */
    private short accessFlags;
    /**
     * 类常量池索引
     */
    private short thisClass;
    /**
     * 父类常量池索引
     * <p>
     * 如果 Class 文件的 super_class 的值为 0，那这个 Class 文件只可能是JDK定义的
     * java.lang.Object 类，只有它是唯一没有父类的类
     */
    private short superClass;
    /**
     * 接口数量
     */
    private short interfaceCount;
    /**
     * 接口常量池索引数组
     */
    private short[] interfaces;
    /**
     * 字段数量
     */
    private short fieldCount;
    /**
     * 字段数组
     */
    private FieldInfo[] fieldInfos;
    /**
     * 方法数量
     */
    private short methodCount;
    /**
     * 方法数组
     */
    private MethodInfo[] methodInfos;
    /**
     * 属性数量
     */
    private short attributeCount;
    /**
     * 属性表
     */
    private AttributeInfo[] attributeInfos;

    public ClassFileReader(InputStream classFileStream) {
        initClassFileInfo(classFileStream);
    }

    public void initClassFileInfo(InputStream classFileStream) {
        try {
            DataInputStream dataInputStream = new DataInputStream(classFileStream);
            magic = dataInputStream.readInt();
            minorVersion = dataInputStream.readShort();
            majorVersion = dataInputStream.readShort();

            constantPoolCount = dataInputStream.readShort();
            constantPool = initConstantPoolInfo(dataInputStream);

            accessFlags = dataInputStream.readShort();
            thisClass = dataInputStream.readShort();
            superClass = dataInputStream.readShort();

            interfaceCount = dataInputStream.readShort();
            interfaces = initInterfaces(dataInputStream);

            fieldCount = dataInputStream.readShort();
            fieldInfos = initFieldInfo(dataInputStream);

            methodCount = dataInputStream.readShort();
            methodInfos = initMethodInfo(dataInputStream, constantPool);

            attributeCount = dataInputStream.readShort();
            attributeInfos = initAttributeInfo(dataInputStream);
        } catch (Exception e) {
            throw new RuntimeException("resolve class file stream failed", e);
        } finally {
            try {
                classFileStream.close();
            } catch (IOException e) {
            }
        }
    }

    private ConstantPool initConstantPoolInfo(DataInputStream dataInputStream) throws Exception {
        ConstantPool constantPool = new ConstantPool(constantPoolCount);
        for (int i = 1; i < constantPool.getConstantInfos().length; i++) {
            ConstantInfo constantInfo = ConstantInfo.read(dataInputStream, constantPool);
            constantPool.getConstantInfos()[i] = constantInfo;
            if (constantInfo.getClass() == ConstantLongInfo.class || constantInfo.getClass() == ConstantDoubleInfo.class) {
                i++;
                constantPool.getConstantInfos()[i] = new ConstantPaddingInfo();
                constantPool.getConstantInfos()[i].setConstantPool(constantPool);
            }
        }
        return constantPool;
    }

    private short[] initInterfaces(DataInputStream dataInputStream) throws Exception {
        short[] interfaces = new short[interfaceCount];
        for (int i = 0; i < interfaces.length; i++) {
            interfaces[i] = dataInputStream.readShort();
        }
        return interfaces;
    }

    private FieldInfo[] initFieldInfo(DataInputStream dataInputStream) throws Exception {
        FieldInfo[] fieldInfos = new FieldInfo[fieldCount];
        for (int i = 0; i < fieldInfos.length; i++) {
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.initFieldInfo(dataInputStream, constantPool);
            fieldInfos[i] = fieldInfo;
        }
        return fieldInfos;
    }

    private MethodInfo[] initMethodInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws Exception {
        MethodInfo[] methodInfos = new MethodInfo[methodCount];
        for (int i = 0; i < methodInfos.length; i++) {
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.initMethodInfo(dataInputStream, constantPool);
            methodInfos[i] = methodInfo;

        }
        return methodInfos;
    }

    private AttributeInfo[] initAttributeInfo(DataInputStream dataInputStream) throws Exception {
        AttributeInfo[] attributeInfos = new AttributeInfo[attributeCount];
        for (int i = 0; i < attributeInfos.length; i++) {
            AttributeInfo attributeInfo = AttributeInfo.read(dataInputStream, constantPool);
            attributeInfos[i] = attributeInfo;
        }
        return attributeInfos;
    }

    public List<ClassModifier> getAccess() {
        return ClassModifier.getModifiers(accessFlags);
    }

    public String getThisClassName() {
        ConstantInfo constantInfo = constantPool.getConstantInfos()[thisClass];
        String name = (String) constantInfo.getBytesValue();
        return name.replace("/", ".");
    }

    public String getSuperClassName() {
        ConstantInfo constantInfo = constantPool.getConstantInfos()[superClass];
        String name = (String) constantInfo.getBytesValue();
        return name.replace("/", ".");
    }

    public List<String> getInterfacesName() {
        List<String> list = Lists.newArrayList();
        for (short interfaceIndex : interfaces) {
            ConstantInfo constantInfo = constantPool.getConstantInfos()[interfaceIndex];
            String name = (String) constantInfo.getBytesValue();
            list.add(name.replace("/", "."));
        }
        return list;
    }

    public int getMagic() {
        return magic;
    }

    public short getMinorVersion() {
        return minorVersion;
    }

    public short getMajorVersion() {
        return majorVersion;
    }

    public short getConstantPoolCount() {
        return constantPoolCount;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public short getAccessFlags() {
        return accessFlags;
    }

    public short getThisClass() {
        return thisClass;
    }

    public short getSuperClass() {
        return superClass;
    }

    public short getInterfaceCount() {
        return interfaceCount;
    }

    public short[] getInterfaces() {
        return interfaces;
    }

    public short getFieldCount() {
        return fieldCount;
    }

    public FieldInfo[] getFieldInfos() {
        return fieldInfos;
    }

    public short getMethodCount() {
        return methodCount;
    }

    public MethodInfo[] getMethodInfos() {
        return methodInfos;
    }

    public short getAttributeCount() {
        return attributeCount;
    }

    public AttributeInfo[] getAttributeInfos() {
        return attributeInfos;
    }
}
