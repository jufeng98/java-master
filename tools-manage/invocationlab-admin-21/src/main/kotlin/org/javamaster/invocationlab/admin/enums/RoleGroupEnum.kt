package org.javamaster.invocationlab.admin.enums

import com.google.common.collect.Sets
import java.util.*
import java.util.stream.Collectors

/**
 * @author yudong
 */
enum class RoleGroupEnum(
    @JvmField val id: Long,
    @JvmField val roleId: String,
    @JvmField val roleCode: String,
    @JvmField val roleName: String
) {
    OWNER(1625737465778208769L, "d2f5628dc54dd72b1dfaa6d15368180b", "590d4773_0", "团队所有者"),
    ADMIN(1625737465786597377L, "8310eb4e2dae9a72ff4d82f8c561841b", "590d4773_1", "团队管理员"),
    ORDINARY(1625737465790791682L, "ab2809bc8d67fd3e78157300b907ef7f", "590d4773_2", "团队普通成员"),
    ;

    companion object {
        @JvmStatic
        fun getRoleGroupEnum(roleId: String): RoleGroupEnum {
            for (value in entries) {
                if (value.roleId == roleId) {
                    return value
                }
            }
            throw RuntimeException("wrong:$roleId")
        }

        @JvmStatic
        fun getDefaultRoleEnums(roleGroupEnum: RoleGroupEnum): Set<Long> {
            when (roleGroupEnum) {
                OWNER -> {
                    return Arrays.stream(RoleEnum.entries.toTypedArray())
                        .map { roleEnum: RoleEnum -> roleEnum.id }
                        .collect(Collectors.toSet())
                }

                ADMIN -> {
                    val ids = Arrays.stream(RoleEnum.entries.toTypedArray())
                        .map { roleEnum: RoleEnum -> roleEnum.id }
                        .collect(Collectors.toSet())
                    ids.remove(RoleEnum.ERD_PROJECT_GROUP_EDIT.id)
                    ids.remove(RoleEnum.ERD_PROJECT_GROUP_DEL.id)
                    return ids
                }

                ORDINARY -> {
                    return Sets.newHashSet(
                        RoleEnum.ERD_PROJECT_VIEW.id, RoleEnum.ERD_TABLE_EXPORT.id,
                        RoleEnum.ERD_TABLE_EXPORT_COMMON.id, RoleEnum.ERD_TABLE_EXPORT_MORE.id
                    )
                }

                else -> {
                    throw RuntimeException("wrong:$roleGroupEnum")
                }
            }
        }
    }
}
