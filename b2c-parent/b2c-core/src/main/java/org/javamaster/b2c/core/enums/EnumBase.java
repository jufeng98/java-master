package org.javamaster.b2c.core.enums;

/**
 * 枚举类的公共接口
 *
 * @author yudong
 * @date 2019/6/10
 */
public interface EnumBase {
    int getCode();

    String getMsg();

    /**
     * 根据code获取对应的枚举对象
     *
     * @param enumClass
     * @param code
     * @param <E>
     * @return 若code为null, 则返回null
     */
    static <E extends Enum<?> & EnumBase> E codeOf(Class<E> enumClass, Integer code) {
        if (code == null) {
            return null;
        }
        E[] enumConstants = enumClass.getEnumConstants();
        for (E e : enumConstants) {
            if (e.getCode() == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("the code didn't match any enum,code:" + code + ",enum class:" + enumClass.getName());
    }

}
