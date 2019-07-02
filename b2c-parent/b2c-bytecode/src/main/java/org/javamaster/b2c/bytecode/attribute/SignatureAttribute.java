package org.javamaster.b2c.bytecode.attribute;


import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;

import java.io.DataInputStream;

/**
 * 描述类,方法,字段的特征签名,为了支持泛型.因为类型擦除之后,描述符无法再描述泛型信息(JDK1.5)
 * 注意:这里并不是擦除前的具体对象类型,而是T、K之类的泛型符号.例如字段:"Map<K, U> data",
 * Signature为"Ljava/util/Map<TK;TU;>;"
 * <p>
 * attributeLength的长度固定为2
 *
 * @author yudong
 * @date 2019/6/26
 */
public class SignatureAttribute extends AttributeInfo {
    private short signatureIndex;

    public SignatureAttribute(short attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void initSubInfo(DataInputStream infoStream) throws Exception {
        signatureIndex = infoStream.readShort();
    }

    public short getSignatureIndex() {
        return signatureIndex;
    }

    public Object getSignature() {
        ConstantInfo constantInfo = constantPool.getConstantInfos()[signatureIndex];
        return constantInfo.getBytesValue();
    }
}
