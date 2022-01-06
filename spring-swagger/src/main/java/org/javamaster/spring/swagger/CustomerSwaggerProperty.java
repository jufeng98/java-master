package org.javamaster.spring.swagger;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ModelSpecificationBuilder;
import springfox.documentation.builders.PropertySpecificationBuilder;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import java.util.Date;

/**
 * 全局修改swagger，所有Date类型显示为long
 *
 * @author yudong
 * @date 2022/1/4
 */
@Component
public class CustomerSwaggerProperty implements ModelPropertyBuilderPlugin {
    @Override
    public void apply(ModelPropertyContext modelPropertyContext) {
        if (!modelPropertyContext.getBeanPropertyDefinition().isPresent()) {
            return;
        }
        BeanPropertyDefinition beanPropertyDefinition = modelPropertyContext.getBeanPropertyDefinition().get();
        PropertySpecificationBuilder builder = modelPropertyContext.getSpecificationBuilder();
        if (beanPropertyDefinition.getRawPrimaryType() == Date.class) {
            builder.type(new ModelSpecificationBuilder().scalarModel(ScalarType.LONG).build());
        }
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return delimiter == DocumentationType.SWAGGER_2;
    }
}
