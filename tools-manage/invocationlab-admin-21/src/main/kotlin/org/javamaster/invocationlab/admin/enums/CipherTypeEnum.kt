package org.javamaster.invocationlab.admin.enums

enum class CipherTypeEnum(@JvmField val type: String) {
    AES("AES"),
    MYSQL_AES("MYSQL-AES"),
    ;

    companion object {
        @JvmStatic
        fun getByType(type: String): CipherTypeEnum {
            for (value in entries) {
                if (value.type == type) {
                    return value
                }
            }
            throw IllegalArgumentException("wrong type:$type")
        }
    }
}
