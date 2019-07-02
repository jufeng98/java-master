package org.javamaster.b2c.bytecode.attribute;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

import java.io.DataInputStream;

/**
 * 描述泛型参数化类型(1.5)
 *
 * @author yudong
 * @date 2019/6/26
 */
public class LocalVariableTypeTableAttribute extends AttributeInfo {
    private short localVariableTypeTableLength;
    private LocalVariableTypeTable[] localVariableTypeTables;

    public LocalVariableTypeTableAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        localVariableTypeTableLength = infoStream.readShort();
        localVariableTypeTables = new LocalVariableTypeTable[localVariableTypeTableLength];
        for (int i = 0; i < localVariableTypeTableLength; i++) {
            localVariableTypeTables[i] = new LocalVariableTypeTable(infoStream.readShort(), infoStream.readShort(),
                    infoStream.readShort(), infoStream.readShort(), infoStream.readShort());
        }
    }

    public short getLocalVariableTypeTableLength() {
        return localVariableTypeTableLength;
    }

    public LocalVariableTypeTable[] getLocalVariableTypeTables() {
        return localVariableTypeTables;
    }

    @JsonPropertyOrder({"startPc", "length", "nameIndex", "name", "signatureIndex", "signature"})
    public class LocalVariableTypeTable {
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
        private short signatureIndex;
        /**
         * 局部变量的slot位置
         */
        private short index;

        public LocalVariableTypeTable(short startPc, short length, short nameIndex, short signatureIndex, short index) {
            this.startPc = startPc;
            this.length = length;
            this.nameIndex = nameIndex;
            this.signatureIndex = signatureIndex;
            this.index = index;
        }

        public Object getName() {
            ConstantInfo constantInfo = constantPool.getConstantInfos()[nameIndex];
            return constantInfo.getBytesValue();
        }

        public Object getSignature() {
            ConstantInfo constantInfo = constantPool.getConstantInfos()[signatureIndex];
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

        public short getSignatureIndex() {
            return signatureIndex;
        }

        public short getIndex() {
            return index;
        }
    }
}
