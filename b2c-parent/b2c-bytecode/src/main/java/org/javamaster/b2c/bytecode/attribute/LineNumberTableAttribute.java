package org.javamaster.b2c.bytecode.attribute;

import java.io.DataInputStream;

/**
 * 描述源码行号和字节码行号的对应关系
 *
 * @author yudong
 * @date 2019/6/26
 */
public class LineNumberTableAttribute extends AttributeInfo {
    private short lineNumberTableLength;
    private LineNumberTable[] lineNumberTables;

    public LineNumberTableAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        lineNumberTableLength = infoStream.readShort();
        lineNumberTables = new LineNumberTable[lineNumberTableLength];
        for (int i = 0; i < lineNumberTableLength; i++) {
            lineNumberTables[i] = new LineNumberTable(infoStream.readShort(), infoStream.readShort());
        }
    }

    public short getLineNumberTableLength() {
        return lineNumberTableLength;
    }

    public LineNumberTable[] getLineNumberTables() {
        return lineNumberTables;
    }


    public static class LineNumberTable {
        /**
         * 字节码行号
         */
        private short startPc;
        /**
         * 源码行号
         */
        private short lineNumber;

        public LineNumberTable(short startPc, short lineNumber) {
            this.startPc = startPc;
            this.lineNumber = lineNumber;
        }

        public short getStartPc() {
            return startPc;
        }

        public short getLineNumber() {
            return lineNumber;
        }

    }
}
