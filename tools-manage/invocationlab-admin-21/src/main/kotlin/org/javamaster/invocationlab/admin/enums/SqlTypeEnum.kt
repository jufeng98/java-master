package org.javamaster.invocationlab.admin.enums

/**
 * @author yudong
 * @date 2023/9/3
 */
enum class SqlTypeEnum(@JvmField val type: String) {
    DELETE("delete"),
    UPDATE("update"),
    INSERT("insert"),
    CREATE("create"),
    DROP("drop"),
    ALTER("alter"),
    ;

    companion object {
        @JvmStatic
        fun getByType(type: String): SqlTypeEnum {
            for (value in entries) {
                if (value.type == type) {
                    return value
                }
            }
            throw IllegalArgumentException(type)
        }
    }
}
