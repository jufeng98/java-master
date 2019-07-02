package org.javamaster.b2c.bytecode.access;


import org.javamaster.b2c.bytecode.modifier.FieldModifier;

import java.util.List;

/**
 * @author yudong
 * @date 2019/1/11
 */
public class FieldAccess {
    private final List<FieldModifier> modifiers;

    public FieldAccess(short accessFlags) {
        modifiers = FieldModifier.getModifiers(accessFlags);
    }

    public List<FieldModifier> getModifiers() {
        return modifiers;
    }

}
