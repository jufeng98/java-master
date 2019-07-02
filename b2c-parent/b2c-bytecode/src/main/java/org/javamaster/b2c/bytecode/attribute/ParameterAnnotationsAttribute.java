package org.javamaster.b2c.bytecode.attribute;

import com.google.common.collect.Lists;
import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

import java.io.DataInputStream;
import java.util.List;

/**
 * @author yudong
 * @date 2019/6/26
 */
public class ParameterAnnotationsAttribute extends AttributeInfo {
    private byte numberOfParameters;
    private ParameterAnnotationTable[] parameterAnnotationTables;

    public ParameterAnnotationsAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        numberOfParameters = infoStream.readByte();
        parameterAnnotationTables = new ParameterAnnotationTable[numberOfParameters];
        for (int i = 0; i < parameterAnnotationTables.length; i++) {
            ParameterAnnotationTable parameterAnnotationTable = new ParameterAnnotationTable();
            try {
                parameterAnnotationTable.initAttribute(infoStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            parameterAnnotationTables[i] = parameterAnnotationTable;
        }
    }

    public byte getNumberOfParameters() {
        return numberOfParameters;
    }

    public ParameterAnnotationTable[] getParameterAnnotationTables() {
        return parameterAnnotationTables;
    }


    public class ParameterAnnotationTable {
        private short numberOfAnnotations;
        private short[] annotations;

        public void initAttribute(DataInputStream stream) throws Exception {
            numberOfAnnotations = stream.readShort();
            if (numberOfAnnotations == 0) {
                return;
            }
            annotations = new short[numberOfAnnotations];
            for (int i = 0; i < annotations.length; i++) {
                annotations[i] = stream.readShort();
            }
        }

        public List<Object> getAnnotationValues() {
            List<Object> list = Lists.newArrayList();
            if (annotations == null) {
                return list;
            }
            for (short annotation : annotations) {
                ConstantInfo constantInfo = constantPool.getConstantInfos()[annotation];
                list.add(constantInfo.getBytesValue());
            }
            return list;
        }

        public short getNumberOfAnnotations() {
            return numberOfAnnotations;
        }

        public short[] getAnnotations() {
            return annotations;
        }
    }
}
