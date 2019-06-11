package org.javamaster.b2c.core.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import org.javamaster.b2c.core.enums.EnumBase;
import org.javamaster.b2c.core.utils.ClassUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实现了EnumBase接口的枚举类对象序列化和反序列化
 *
 * @author yudong
 * @date 2019/6/10
 */
public class EnumBaseModule extends SimpleModule {

    public EnumBaseModule() {
        super(PackageVersion.VERSION);
        // 找到EnumBase接口所在的包下所有实现该接口的枚举类
        Set<Class> set = ClassUtils.getAllClassesFromPackage(EnumBase.class.getPackage().getName())
                .stream()
                .filter(clz -> clz.isEnum() && EnumBase.class.isAssignableFrom(clz))
                .collect(Collectors.toSet());
        // 动态注册所有实现了EnumBase接口枚举类的序列化器和反序列化器到Jackson
        set.forEach(enumClass -> {
            addDeserializer(enumClass, new EnumBaseDeserializer(enumClass));
            addSerializer(enumClass, new EnumBaseSerializer());

        });
    }

}
