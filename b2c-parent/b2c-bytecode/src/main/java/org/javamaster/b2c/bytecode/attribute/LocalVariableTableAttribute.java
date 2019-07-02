package org.javamaster.b2c.bytecode.attribute;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

import java.io.DataInputStream;

/**
 * 描述栈帧中局部变量表中的变量与源码中定义的变量之间的关系
 *
 * @author yudong
 * @date 2019/6/26
 */
public class LocalVariableTableAttribute extends AttributeInfo {
    private short localVariableTableLength;
    private LocalVariableInfo[] localVariableTables;

    public LocalVariableTableAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        localVariableTableLength = infoStream.readShort();
        localVariableTables = new LocalVariableInfo[localVariableTableLength];
        for (int i = 0; i < localVariableTableLength; i++) {
            localVariableTables[i] = new LocalVariableInfo(infoStream.readShort(), infoStream.readShort(), infoStream.readShort(),
                    infoStream.readShort(), infoStream.readShort());
        }
    }

    public short getLocalVariableTableLength() {
        return localVariableTableLength;
    }

    public LocalVariableInfo[] getLocalVariableTables() {
        return localVariableTables;
    }

    @JsonPropertyOrder({"startPc", "length", "nameIndex", "name", "descriptorIndex", "descriptor"})
    public class LocalVariableInfo {
        /**
         * 字节码偏移量
         */
        private short startPc;
        /**
         * 作用范围覆盖的长度
         */
        private short length;
        /**
         * 局部变量名称索引下标
         */
        private short nameIndex;
        /**
         * 局部变量描述符索引下标
         */
        private short descriptorIndex;
        /**
         * 局部变量的slot位置
         */
        private short index;

        public LocalVariableInfo(short startPc, short length, short nameIndex, short descriptorIndex, short index) {
            this.startPc = startPc;
            this.length = length;
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
            this.index = index;
        }

        public Object getName() {
            ConstantInfo constantInfo = constantPool.getConstantInfos()[nameIndex];
            return constantInfo.getBytesValue();
        }

        public Object getDescriptor() {
            ConstantInfo constantInfo = constantPool.getConstantInfos()[descriptorIndex];
            return constantInfo.getBytesValue();
        }

        public short getStartPc() {
            return startPc;
        }

        public short getLength() {
            return length;
        }

        public short getNameIndex() {
            return nameIndex;
        }

        public short getDescriptorIndex() {
            return descriptorIndex;
        }

        public short getIndex() {
            return index;
        }
    }
}
