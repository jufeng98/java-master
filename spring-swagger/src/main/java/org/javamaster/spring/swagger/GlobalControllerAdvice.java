package org.javamaster.spring.swagger;

import org.javamaster.spring.swagger.convert.SexEnumFormatter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * @author yudong
 * @date 2022/4/18
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @InitBinder
    public void binder(WebDataBinder dataBinder) {
        dataBinder.addCustomFormatter(new SexEnumFormatter());

        // WebConversionService conversionService = (WebConversionService) dataBinder.getConversionService();

        // conversionService.addConverter(new SexEnumConvert());

        // conversionService.addConverter(new SexEnumGenericConverter());
    }

}
