package org.javamaster.b2c.bytecode.enums;

import org.javamaster.b2c.bytecode.tablestructure.ConstantClassInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantDoubleInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantFieldRefInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantFloatInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantIntegerInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantInterfaceMethodRefInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantInvokeDynamicInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantLongInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantMethodHandleInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantMethodRefInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantMethodTypeInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantNameAndTypeInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantNullInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantStringInfo;
import org.javamaster.b2c.bytecode.tablestructure.ConstantUtf8Info;

import java.util.HashMap;

/**
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public enum ConstantTypeEnum {

    EMPTY((byte) 0, ConstantNullInfo.class),
    UTF8((byte) 1, ConstantUtf8Info.class),
    INTEGER((byte) 3, ConstantIntegerInfo.class),
    FLOAT((byte) 4, ConstantFloatInfo.class),
    LONG((byte) 5, ConstantLongInfo.class),
    DOUBLE((byte) 6, ConstantDoubleInfo.class),
    CLASS((byte) 7, ConstantClassInfo.class),
    STRING((byte) 8, ConstantStringInfo.class),
    FIELD_REF((byte) 9, ConstantFieldRefInfo.class),
    METHOD_REF((byte) 10, ConstantMethodRefInfo.class),
    INTERFACE_METHOD_REF((byte) 11, ConstantInterfaceMethodRefInfo.class),
    NAME_AND_TYPE((byte) 12, ConstantNameAndTypeInfo.class),
    METHOD_HANDLE((byte) 15, ConstantMethodHandleInfo.class),
    METHOD_TYPE((byte) 16, ConstantMethodTypeInfo.class),
    INVOKE_DYNAMIC((byte) 18, ConstantInvokeDynamicInfo.class);

    private final static HashMap<Byte, ConstantTypeEnum> HASH_MAP;

    static {
        HASH_MAP = new HashMap<>();
        for (ConstantTypeEnum constantTypeEnum : ConstantTypeEnum.values()) {
            HASH_MAP.put(constantTypeEnum.tag, constantTypeEnum);
        }
    }

    private final byte tag;
    private final Class<? extends ConstantInfo> clazz;

    ConstantTypeEnum(byte tag, Class<? extends ConstantInfo> clazz) {
        this.tag = tag;
        this.clazz = clazz;
    }

    public static ConstantTypeEnum getConstantType(byte tag) {
        ConstantTypeEnum typeEnum = HASH_MAP.get(tag);
        if (typeEnum == null) {
            throw new RuntimeException("unknown tag:" + tag);
        }
        return typeEnum;
    }

    public byte getTag() {
        return tag;
    }

    public Class<? extends ConstantInfo> getClazz() {
        return clazz;
    }

    public ConstantInfo newInstance() {
        try {
            return this.clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
