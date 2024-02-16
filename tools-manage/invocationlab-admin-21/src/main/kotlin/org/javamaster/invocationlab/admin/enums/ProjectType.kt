package org.javamaster.invocationlab.admin.enums

/**
 * @author yudong
 * @date 2022/11/8
 */
enum class ProjectType(@JvmField val type: Int) {
    PERSONAL(1),
    GROUP(2),
    ;

    companion object {
        @JvmStatic
        fun getByType(type: Int?): ProjectType {
            if (type == null) {
                return GROUP
            }
            for (value in entries) {
                if (value.type == type) {
                    return value
                }
            }
            return GROUP
        }
    }
}
