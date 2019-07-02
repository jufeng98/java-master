package org.javamaster.b2c.bytecode.tablestructure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.javamaster.b2c.bytecode.ConstantPool;
import org.javamaster.b2c.bytecode.enums.ConstantTypeEnum;
import org.javamaster.b2c.bytecode.jackson.TagSerializer;

import java.io.DataInputStream;

/**
 * @author yudong
 * @date 2019/1/11
 */
public abstract class ConstantInfo {
    /**
     * 常量类型
     */
    @JsonSerialize(using = TagSerializer.class)
    private byte tag;

    @JsonIgnore
    protected ConstantPool constantPool;

    public static ConstantInfo read(DataInputStream dataInputStream, ConstantPool constantPool) throws Exception {
        byte tag = dataInputStream.readByte();
        ConstantInfo constantInfo = ConstantTypeEnum.getConstantType(tag).newInstance();
        constantInfo.tag = tag;
        constantInfo.constantPool = constantPool;
        constantInfo.initConstantInfo(dataInputStream);
        return constantInfo;
    }

    /**
     * 初始化
     *
     * @param dataInputStream
     * @throws Exception
     */
    public abstract void initConstantInfo(DataInputStream dataInputStream) throws Exception;

    /**
     * 获取值
     *
     * @return
     */
    public abstract Object getBytesValue();

    public void setConstantPool(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    public byte getTag() {
        return tag;
    }

}
