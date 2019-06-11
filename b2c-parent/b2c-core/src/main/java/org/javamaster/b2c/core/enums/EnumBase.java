package org.javamaster.b2c.core.enums;

import org.javamaster.b2c.core.exception.BizException;

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
     * @return 若无对应的枚举对象, 返回null
     */
    static <E extends Enum<?> & EnumBase> E codeOf(Class<E> enumClass, int code) {
        E[] enumConstants = enumClass.getEnumConstants();
        for (E e : enumConstants) {
            if (e.getCode() == code) {
                return e;
            }
        }
        throw new BizException(BizExceptionEnum.INVALID_REQ_PARAM);
    }

}
