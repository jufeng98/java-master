package org.javamaster.b2c.bytecode.tablestructure;

import java.io.DataInputStream;

/**
 * 用作常量池第0位占位符,第0位由虚拟机保留
 * <p>
 * Created on 2019/1/11.<br/>
 *
 * @author yudong
 */
public class ConstantNullInfo extends ConstantInfo {

    @Override
    public void initConstantInfo(DataInputStream dataInputStream) {

    }

    @Override
    public Object getBytesValue() {
        return null;
    }
}
