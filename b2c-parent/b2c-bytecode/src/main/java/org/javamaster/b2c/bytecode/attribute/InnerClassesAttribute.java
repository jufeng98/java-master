package org.javamaster.b2c.bytecode.attribute;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.javamaster.b2c.bytecode.modifier.ClassModifier;
import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

import java.io.DataInputStream;
import java.util.List;

/**
 * 描述内部类和宿主类之间的关系
 *
 * @author yudong
 * @date 2019/6/26
 */
public class InnerClassesAttribute extends AttributeInfo {
    private short numberOfClasses;
    private InnerClassesInfo[] innerClasses;

    public InnerClassesAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        numberOfClasses = infoStream.readShort();
        innerClasses = new InnerClassesInfo[numberOfClasses];
        for (int i = 0; i < numberOfClasses; i++) {
            innerClasses[i] = new InnerClassesInfo(infoStream.readShort(), infoStream.readShort(),
                    infoStream.readShort(), infoStream.readShort());
        }
    }

    public short getNumberOfClasses() {
        return numberOfClasses;
    }

    public InnerClassesInfo[] getInnerClasses() {
        return innerClasses;
    }

    @JsonPropertyOrder({"innerClassInfoIndex", "innerClassInfo", "outerClassInfoIndex", "outerClassInfo",
            "innerNameIndex", "innerName"})
    private class InnerClassesInfo {
        /**
         * 内部类的符号引用索引下标
         */
        private short innerClassInfoIndex;
        /**
         * 宿主类的符号引用索引下标
         */
        private short outerClassInfoIndex;
        /**
         * 内部类名称索引下标,匿名内部类此项为0
         */
        private short innerNameIndex;
        /**
         * 内部类访问标志
         */
        private short innerClassAccessFlags;

        public InnerClassesInfo(short innerClassInfoIndex, short outerClassInfoIndex, short innerNameIndex, short innerClassAccessFlags) {
            this.innerClassInfoIndex = innerClassInfoIndex;
            this.outerClassInfoIndex = outerClassInfoIndex;
            this.innerNameIndex = innerNameIndex;
            this.innerClassAccessFlags = innerClassAccessFlags;
        }

        public Object getInnerClassInfo() {
            ConstantInfo constantInfo = constantPool.getConstantInfos()[innerClassInfoIndex];
            return constantInfo.getBytesValue();
        }

        public Object getOuterClassInfo() {
            if (outerClassInfoIndex == 0) {
                return "";
            }
            ConstantInfo constantInfo = constantPool.getConstantInfos()[outerClassInfoIndex];
            return constantInfo.getBytesValue();
        }

        public Object getInnerName() {
            if (innerNameIndex == 0) {
                return "";
            }
            ConstantInfo constantInfo = constantPool.getConstantInfos()[innerNameIndex];
            return constantInfo.getBytesValue();
        }

        public List<ClassModifier> getInnerClassAccess() {
            return ClassModifier.getModifiers(innerClassAccessFlags);
        }

        public short getInnerClassInfoIndex() {
            return innerClassInfoIndex;
        }

        public short getOuterClassInfoIndex() {
            return outerClassInfoIndex;
        }

        public short getInnerNameIndex() {
            return innerNameIndex;
        }

        public short getInnerClassAccessFlags() {
            return innerClassAccessFlags;
        }

    }
}
