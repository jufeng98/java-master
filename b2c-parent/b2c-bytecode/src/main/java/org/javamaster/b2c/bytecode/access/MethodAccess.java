package org.javamaster.b2c.bytecode.access;


import org.javamaster.b2c.bytecode.modifier.MethodModifier;

import java.util.List;

/**
 * @author yudong
 * @date 2019/1/11
 */
public class MethodAccess {
    private final List<MethodModifier> modifiers;

    public MethodAccess(short accessFlags) {
        modifiers = MethodModifier.getModifiers(accessFlags);
    }

    public List<MethodModifier> getModifiers() {
        return modifiers;
    }
}
