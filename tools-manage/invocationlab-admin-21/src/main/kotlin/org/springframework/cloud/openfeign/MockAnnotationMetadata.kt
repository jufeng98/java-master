package org.springframework.cloud.openfeign

import com.google.common.collect.Maps
import org.springframework.core.type.StandardAnnotationMetadata

/**
 * @author yudong
 * @date 2022/11/12
 */
class MockAnnotationMetadata(introspectedClass: Class<*>, private val feignServicePackages: List<String>) :
    StandardAnnotationMetadata(
        introspectedClass
    ) {
    override fun getAnnotationAttributes(annotationName: String): Map<String, Any> {
        val map: MutableMap<String, Any> = Maps.newHashMap()
        map["value"] = feignServicePackages.toTypedArray<String>()
        map["basePackages"] = arrayOfNulls<String>(0)
        map["basePackageClasses"] = arrayOfNulls<Class<*>>(0)
        map["defaultConfiguration"] = arrayOfNulls<Class<*>>(0)
        map["clients"] = arrayOfNulls<Class<*>>(0)
        return map
    }
}
