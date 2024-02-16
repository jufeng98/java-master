package org.javamaster.invocationlab.admin.annos

import org.javamaster.invocationlab.admin.enums.RoleEnum

/**
 * @author yudong
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class ErdRolesAllowed(vararg val value: RoleEnum, val msg: String = "无权操作")
