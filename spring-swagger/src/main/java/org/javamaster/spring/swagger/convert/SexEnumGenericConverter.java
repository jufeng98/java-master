package org.javamaster.spring.swagger.convert;

import com.google.common.collect.Sets;
import org.javamaster.spring.swagger.enums.SexEnum;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Set;

/**
 * @author yudong
 * @date 2022/4/20
 */
public class SexEnumGenericConverter implements GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Sets.newHashSet(new ConvertiblePair(String.class, SexEnum.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return SexEnum.getByCode(source.toString());
    }
}
