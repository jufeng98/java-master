package org.javamaster.spring.swagger.convert;

import org.javamaster.spring.swagger.enums.SexEnum;
import org.springframework.core.convert.converter.Converter;

/**
 * @author yudong
 * @date 2022/4/20
 */
public class SexEnumConvert implements Converter<String, SexEnum> {
    @Override
    public SexEnum convert(String source) {
        return SexEnum.getByCode(source);
    }
}
