package org.javamaster.invocationlab.admin.enums

/**
 * @author yudong
 * @date 2022/11/8
 */
enum class RegisterCenterType(@JvmField val type: Int) {
    ZK(1),
    EUREKA(2),
    ;

    companion object {
        @JvmStatic
        fun getByType(type: Int?): RegisterCenterType? {
            if (type == null) {
                return ZK
            }
            for (value in entries) {
                if (value.type == type) {
                    return value
                }
            }
            return null
        }
    }
}
