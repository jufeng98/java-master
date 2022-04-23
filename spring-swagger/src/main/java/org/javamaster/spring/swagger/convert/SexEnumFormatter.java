package org.javamaster.spring.swagger.convert;

import org.javamaster.spring.swagger.enums.SexEnum;
import org.springframework.format.Formatter;

import java.util.Locale;

/**
 * @author yudong
 * @date 2022/4/20
 */
public class SexEnumFormatter implements Formatter<SexEnum> {
    @Override
    public SexEnum parse(String s, Locale locale) {
        return SexEnum.getByCode(s);
    }

    @Override
    public String print(SexEnum o, Locale locale) {
        return o.code.toString();
    }
}
