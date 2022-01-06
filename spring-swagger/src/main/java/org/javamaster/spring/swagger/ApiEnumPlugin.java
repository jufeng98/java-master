package org.javamaster.spring.swagger;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.javamaster.spring.swagger.anno.ApiEnum;
import org.javamaster.spring.swagger.enums.EnumBase;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import springfox.documentation.builders.PropertySpecificationBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author yudong
 * @date 2022/1/4
 */
@Component
public class ApiEnumPlugin implements ModelPropertyBuilderPlugin {

    @SneakyThrows
    @Override
    public void apply(ModelPropertyContext modelPropertyContext) {
        Optional<BeanPropertyDefinition> optional = modelPropertyContext.getBeanPropertyDefinition();
        if (!optional.isPresent()) {
            return;
        }
        BeanPropertyDefinition beanPropertyDefinition = optional.get();
        ApiEnum apiEnum = beanPropertyDefinition.getField().getAnnotation(ApiEnum.class);
        if (apiEnum == null) {
            return;
        }
        Class<? extends Enum<? extends EnumBase>> value = apiEnum.value();
        Enum<? extends EnumBase>[] enumConstants = value.getEnumConstants();
        List<String> list = Arrays.stream(enumConstants)
                .map(enumConstant -> {
                    EnumBase enumBase = (EnumBase) enumConstant;
                    if (StringUtils.isBlank(enumBase.getText())) {
                        return null;
                    }
                    return enumBase.getCode() + ":" + enumBase.getText();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        PropertySpecificationBuilder builder = modelPropertyContext.getSpecificationBuilder();
        Field field = ReflectionUtils.findField(builder.getClass(), "description");
        ReflectionUtils.makeAccessible(Objects.requireNonNull(field));
        String desc = (String) field.get(builder);
        String enumDesc = StringUtils.join(list, "；");
        desc = desc + "，" + enumDesc;
        builder.description(desc);
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return DocumentationType.SWAGGER_2 == delimiter;
    }
}
