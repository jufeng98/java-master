package org.javamaster.b2c.bytecode.access;


import org.javamaster.b2c.bytecode.modifier.ClassModifier;

import java.util.List;


/**
 * @author yudong
 * @date 2019/1/11
 */
public class ClassAccess {
    private final List<ClassModifier> modifiers;

    public ClassAccess(short accessFlags) {
        modifiers = ClassModifier.getModifiers(accessFlags);
    }

    public List<ClassModifier> getModifiers() {
        return modifiers;
    }

}
