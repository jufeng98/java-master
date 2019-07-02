package org.javamaster.b2c.bytecode.attribute;

import java.io.DataInputStream;

/**
 * @author yudong
 * @date 2019/6/26
 */
public class AnnotationAttributeValue {
    protected byte tag;

    public AnnotationAttributeValue(byte tag) {
        this.tag = tag;
    }

    public static AnnotationAttributeValue read(DataInputStream infoStream) throws Exception {
        byte var1 = infoStream.readByte();
        switch (var1) {
            case '@':
                AnnotationIndexTable annotationIndexTable = new AnnotationIndexTable();
                annotationIndexTable.initAttribute(infoStream, null);
                return new AnnoAnnotationAttributeValue(var1, annotationIndexTable);
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                return new PrimitiveAnnotationAttributeValue(var1, infoStream.readShort());
            case '[':
                short s = infoStream.readShort();
                AnnotationAttributeValue[] annotationAttributeValues = new AnnotationAttributeValue[s];
                for (int i = 0; i < annotationAttributeValues.length; i++) {
                    annotationAttributeValues[i] = AnnoAnnotationAttributeValue.read(infoStream);
                }
                return new ArrayAnnotationAttributeValue(var1, s, annotationAttributeValues);
            case 'c':
                return new ClassAnnotationAttributeValue(var1, infoStream.readShort());
            case 'e':
                return new EnumAnnotationAttributeValue(var1, infoStream.readShort(), infoStream.readShort());
            default:
                throw new RuntimeException("unknown tag");
        }

    }

    public byte getTag() {
        return tag;
    }

    public static class PrimitiveAnnotationAttributeValue extends AnnotationAttributeValue {
        private short constValueIndex;

        public PrimitiveAnnotationAttributeValue(byte tag, short constValueIndex) {
            super(tag);
            this.constValueIndex = constValueIndex;
        }

        public short getConstValueIndex() {
            return constValueIndex;
        }

    }

    public static class ClassAnnotationAttributeValue extends AnnotationAttributeValue {
        private short classInfoIndex;

        public ClassAnnotationAttributeValue(byte tag, short classInfoIndex) {
            super(tag);
            this.classInfoIndex = classInfoIndex;
        }

        public short getClassInfoIndex() {
            return classInfoIndex;
        }

    }

    public static class EnumAnnotationAttributeValue extends AnnotationAttributeValue {
        private short typeNameIndex;
        private short constNameIndex;

        public EnumAnnotationAttributeValue(byte tag, short typeNameIndex, short constNameIndex) {
            super(tag);
            this.typeNameIndex = typeNameIndex;
            this.constNameIndex = constNameIndex;
        }

        public short getTypeNameIndex() {
            return typeNameIndex;
        }

        public short getConstNameIndex() {
            return constNameIndex;
        }

    }

    public static class ArrayAnnotationAttributeValue extends AnnotationAttributeValue {
        private short numValues;
        private AnnotationAttributeValue[] annotationAttributeValues;

        public ArrayAnnotationAttributeValue(byte tag, short numValues, AnnotationAttributeValue[] annotationAttributeValues) {
            super(tag);
            this.numValues = numValues;
            this.annotationAttributeValues = annotationAttributeValues;
        }

        public short getNumValues() {
            return numValues;
        }

        public AnnotationAttributeValue[] getAnnotationAttributeValues() {
            return annotationAttributeValues;
        }
    }

    public static class AnnoAnnotationAttributeValue extends AnnotationAttributeValue {
        private AnnotationIndexTable annotationIndexTable;

        public AnnoAnnotationAttributeValue(byte tag, AnnotationIndexTable annotationIndexTable) {
            super(tag);
            this.annotationIndexTable = annotationIndexTable;
        }

        public AnnotationIndexTable getAnnotationIndexTable() {
            return annotationIndexTable;
        }

    }
}
