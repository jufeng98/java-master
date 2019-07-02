package org.javamaster.b2c.bytecode.attribute;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.javamaster.b2c.bytecode.ConstantPool;
import org.javamaster.b2c.bytecode.consts.InstructionConst;
import org.javamaster.b2c.bytecode.jackson.ByteArraySerializer;
import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
@JsonPropertyOrder({"attributeNameIndex", "attributeName", "attributeLength", "info", "maxStack", "maxLocals",
        "codeLength", "code", "codeInstructions"})
public class CodeAttribute extends AttributeInfo {
    /**
     * 操作数栈在运行执行的任何时间点的最大深度
     */
    private short maxStack;
    /**
     * 当前方法引用的局部变量表中的局部变量个数
     */
    private short maxLocals;
    /**
     * 长度必须大于0
     */
    private int codeLength;
    /**
     * code[]数组不能为空,实现当前方法的 Java 虚拟机字节码
     */
    @JsonSerialize(using = ByteArraySerializer.class)
    private byte[] code;
    /**
     * 异常处理器数组长度
     */
    private short exceptionTableLength;
    /**
     * 异常处理器数组
     */
    private ExceptionTable[] exceptionTable;
    /**
     * 给出了 attributeInfos 数组的成员个数。
     */
    private short attributeCount;
    /**
     * 属性表
     */
    private AttributeInfo[] attributeInfos;

    public CodeAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initAttributeInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws Exception {
        attributeLength = dataInputStream.readInt();

        maxStack = dataInputStream.readShort();
        maxLocals = dataInputStream.readShort();
        codeLength = dataInputStream.readInt();

        code = new byte[codeLength];
        for (int i = 0; i < code.length; i++) {
            code[i] = dataInputStream.readByte();
        }
        exceptionTableLength = dataInputStream.readShort();
        exceptionTable = new ExceptionTable[exceptionTableLength];
        for (int i = 0; i < exceptionTable.length; i++) {
            ExceptionTable exceTable = new ExceptionTable();
            exceTable.initExceptionTable(dataInputStream, constantPool);
            exceptionTable[i] = exceTable;
        }
        attributeCount = dataInputStream.readShort();
        attributeInfos = new AttributeInfo[attributeCount];
        for (int i = 0; i < attributeInfos.length; i++) {
            attributeInfos[i] = AttributeInfo.read(dataInputStream, constantPool);
        }
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) {

    }

    public List<String> getCodeInstructions() {
        Field[] fields = InstructionConst.class.getDeclaredFields();
        return IntStream.range(0, code.length)
                .mapToObj(i -> {
                    short value = (short) Byte.toUnsignedInt(code[i]);
                    for (Field field : fields) {
                        try {
                            if ((short) (field.get(null)) == value) {
                                return i + ":" + field.getName();
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return "";
                })
                .collect(Collectors.toList());
    }

    public short getMaxStack() {
        return maxStack;
    }

    public short getMaxLocals() {
        return maxLocals;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public byte[] getCode() {
        return code;
    }

    public short getExceptionTableLength() {
        return exceptionTableLength;
    }

    public ExceptionTable[] getExceptionTable() {
        return exceptionTable;
    }

    public short getAttributeCount() {
        return attributeCount;
    }

    public AttributeInfo[] getAttributeInfos() {
        return attributeInfos;
    }

    public static class ExceptionTable {
        @JsonIgnore
        protected ConstantPool constantPool;
        /**
         * 异常处理器在 code[]数组中的起始范围
         */
        private short startPc;
        /**
         * 异常处理器在 code[]数组中的截止范围
         */
        private short endPc;
        /**
         * 表示一个异常处理器的起点，它的值必须同时是一个对当前 code[]数组中某一指令的操作码的有效索引。
         */
        private short handlerPc;
        /**
         * 如果 catch_type 项的值不为 0，那么它必须是对常量池的一个有效索引，常量池
         * 在该索引处的项必须是 CONSTANT_Class_info结构，表示当前异常
         * 处理器指定需要捕捉的异常类型。只有当抛出的异常是指定的类或其子类的实例时，
         * 异常处理器才会被调用。
         * 如果 catch_type 项的值如果为 0，那么这个异常处理器将会在所有异常抛出时都
         * 被调用。这可以用于实现 finally 语句
         */
        private short catchType;

        public void initExceptionTable(DataInputStream dataInputStream, ConstantPool constantPool) throws Exception {
            this.constantPool = constantPool;
            startPc = dataInputStream.readShort();
            endPc = dataInputStream.readShort();
            handlerPc = dataInputStream.readShort();
            catchType = dataInputStream.readShort();
        }

        private Object getCatchTypeName() {
            if (catchType == 0) {
                return "";
            }
            ConstantInfo constantInfo = constantPool.getConstantInfos()[catchType];
            return constantInfo.getBytesValue();
        }

        public short getStartPc() {
            return startPc;
        }

        public short getEndPc() {
            return endPc;
        }

        public short getHandlerPc() {
            return handlerPc;
        }

        public short getCatchType() {
            return catchType;
        }
    }
}
