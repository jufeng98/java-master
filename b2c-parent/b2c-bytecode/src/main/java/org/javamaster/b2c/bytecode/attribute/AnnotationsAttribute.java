package org.javamaster.b2c.bytecode.attribute;

import java.io.DataInputStream;

/**
 * @author yudong
 * @date 2019/6/26
 */
public class AnnotationsAttribute extends AttributeInfo {
    private short numberOfAnnotations;
    private AnnotationIndexTable[] annotationIndexTables;

    public AnnotationsAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        numberOfAnnotations = infoStream.readShort();
        annotationIndexTables = new AnnotationIndexTable[numberOfAnnotations];
        for (int i = 0; i < annotationIndexTables.length; i++) {
            AnnotationIndexTable annotationIndexTable = new AnnotationIndexTable();
            annotationIndexTable.initAttribute(infoStream, constantPool);
            annotationIndexTables[i] = annotationIndexTable;
        }
    }

    public short getNumberOfAnnotations() {
        return numberOfAnnotations;
    }

    public AnnotationIndexTable[] getAnnotationIndexTables() {
        return annotationIndexTables;
    }
}
