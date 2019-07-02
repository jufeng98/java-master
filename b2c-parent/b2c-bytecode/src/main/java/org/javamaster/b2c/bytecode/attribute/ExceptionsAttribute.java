package org.javamaster.b2c.bytecode.attribute;

import com.google.common.collect.Lists;
import org.javamaster.b2c.bytecode.tablestructure.ConstantClassInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantUtf8Info;

import java.io.DataInputStream;
import java.util.List;

/**
 * 描述方法可能抛出的异常,也就是throws关键字后面的异常
 *
 * @author yudong
 * @date 2019/6/26
 */
public class ExceptionsAttribute extends AttributeInfo {
    private short numberOfExceptions;
    private short[] exceptionIndexTables;

    public ExceptionsAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        numberOfExceptions = infoStream.readShort();
        exceptionIndexTables = new short[numberOfExceptions];
        for (int i = 0; i < numberOfExceptions; i++) {
            exceptionIndexTables[i] = infoStream.readShort();
        }
    }

    public List<String> getExceptionIndexTableNames() {
        List<String> list = Lists.newArrayList();
        for (int i = 0; i < exceptionIndexTables.length; i++) {
            ConstantClassInfo constantClassInfo = (ConstantClassInfo) constantPool.getConstantInfos()[exceptionIndexTables[i]];
            ConstantUtf8Info constantUtf8Info = (ConstantUtf8Info) constantPool.getConstantInfos()[constantClassInfo.getIndex()];
            list.add(constantUtf8Info.getBytesValue());
        }
        return list;
    }

    public short getNumberOfExceptions() {
        return numberOfExceptions;
    }

    public short[] getExceptionIndexTables() {
        return exceptionIndexTables;
    }
}
