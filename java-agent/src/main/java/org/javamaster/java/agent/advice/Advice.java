package org.javamaster.java.agent.advice;

/**
 * @author yudong
 * @date 2020/10/16
 */
public class Advice {

    private final ClassLoader loader;
    private final Class<?> clazz;
    private final Object target;
    private final Object[] params;
    private final Object returnObj;
    private final Boolean isThrow;

    public Advice(ClassLoader loader, Class<?> clazz, Object target, Object[] params, Object returnObj, Boolean isThrow) {
        this.loader = loader;
        this.clazz = clazz;
        this.target = target;
        this.params = params;
        this.returnObj = returnObj;
        this.isThrow = isThrow;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object getTarget() {
        return target;
    }

    public Object[] getParams() {
        return params;
    }

    public Object getReturnObj() {
        return returnObj;
    }

    public Boolean getThrow() {
        return isThrow;
    }
}
