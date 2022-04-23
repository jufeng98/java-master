package org.javamaster.spring.swagger.controller;

import org.javamaster.spring.swagger.enums.Sex;
import org.javamaster.spring.swagger.enums.SexEnum;
import org.javamaster.spring.swagger.model.Result;
import org.javamaster.spring.swagger.model.TestMultipartParam;
import org.javamaster.spring.swagger.model.TestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Part;
import java.util.Arrays;

/**
 * @author yudong
 * @date 2022/4/18
 */
@Controller
@RequestMapping("/param")
public class ParamController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // 请求参数:
    // Content-Type: application/x-www-form-urlencoded
    //
    // age=18&age1=19&ages=20&ages=21&ages1=22&ages1=23&sex=MAN&sexEnum=1
    // 简化后的参数处理路径:
    // RequestParamMethodArgumentResolver->SimpleTypeConverter->WebConversionService->StringToNumberConverterFactory->StringToNumber
    // RequestParamMethodArgumentResolver->SimpleTypeConverter->WebConversionService->StringToNumberConverterFactory->StringToNumber
    // RequestParamMethodArgumentResolver->SimpleTypeConverter->WebConversionService->StringToNumberConverterFactory->ArrayToArrayConverter
    // RequestParamMethodArgumentResolver->SimpleTypeConverter->WebConversionService->StringToNumberConverterFactory->ArrayToArrayConverter
    // RequestParamMethodArgumentResolver->SimpleTypeConverter->WebConversionService->StringToEnumConverterFactory->StringToEnum
    // 该参数走的是自定义转换器,如果用的是formatterRegistry.addFormatter(new SexEnumFormatter())或dataBinder.addCustomFormatter(new SexEnumFormatter())
    // 则参数处理路径: RequestParamMethodArgumentResolver->SimpleTypeConverter->FormatterPropertyEditorAdapter->SexEnumFormatter
    // 如果用的是formatterRegistry.addConverter(new SexEnumConvert())或conversionService.addConverter(new SexEnumConvert())
    // 则参数处理路径: RequestParamMethodArgumentResolver->SimpleTypeConverter->WebConversionService->SexEnumConvert
    @ResponseBody
    @PostMapping("/test")
    public Result<String> test(Integer age,
                               int age1,
                               Integer[] ages,
                               int[] ages1,
                               Sex sex,
                               SexEnum sexEnum) {
        // req:18 19 [20, 21] [22, 23] MAN MAN
        logger.info("req:{} {} {} {} {} {}", age, age1, Arrays.toString(ages), Arrays.toString(ages1), sex, sexEnum);
        return Result.success("");
    }

    // 请求参数:
    // Content-Type: application/x-www-form-urlencoded
    //
    // age=18&age1=19&ages=20&ages=21&ages1=22&ages1=23&sex=MAN
    // 简化后的参数处理路径:
    // ModelAttributeMethodProcessor->BeanWrapperImpl->WebConversionService->...(Bean里的属性填充同简单参数处理)
    @ResponseBody
    @PostMapping("/test1")
    public Result<String> test1(TestParam testParam) {
        // req:TestParam{age=18, age1=19, ages=[20, 21], ages1=[22, 23], sex=MAN}
        logger.info("req:{}", testParam);
        return Result.success("");
    }

    // 请求参数:
    // Content-Type: application/json
    //
    // {"age": 18,"ages": [20,21],"ages1": [22,23],"sex": "MAN"}
    // 简化后的参数处理路径:
    // RequestResponseBodyMethodProcessor->MappingJackson2HttpMessageConverter->ObjectMapper
    @ResponseBody
    @PostMapping("/test2")
    public Result<String> test2(@RequestBody TestParam testParam) {
        // req:TestParam{age=18, age1=19, ages=[20, 21], ages1=[22, 23], sex=MAN}
        logger.info("req:{}", testParam);
        return Result.success("");
    }

    // 请求参数:
    // Content-Type: multipart/form-data; boundary=WebAppBoundary
    // --WebAppBoundary
    // Content-Disposition: form-data; name="file"; filename="README.md"
    //
    // < README.md
    // --WebAppBoundary--
    // 简化后的参数处理路径:
    // RequestPartMethodArgumentResolver->MultipartResolutionDelegate->MultipartHttpServletRequest
    // RequestPartMethodArgumentResolver->MultipartResolutionDelegate->HttpServletRequest
    @ResponseBody
    @PostMapping("/test3")
    public Result<String> test3(@RequestPart("file") MultipartFile multipartFile,
                                @RequestPart("file") Part part) {
        // req:org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile@2c79f164
        // req:org.apache.catalina.core.ApplicationPart@248d5d98
        logger.info("req:{}", multipartFile);
        logger.info("req:{}", part);
        return Result.success("");
    }

    // 请求参数:
    // Content-Type: multipart/form-data; boundary=WebAppBoundary
    //
    // --WebAppBoundary
    // Content-Disposition: form-data; name="age"
    //
    // 18
    // --WebAppBoundary
    // Content-Disposition: form-data; name="file"; filename="README.md"
    //
    // < README.md
    // --WebAppBoundary--
    // 简化后的参数处理路径:
    // RequestParamMethodArgumentResolver->SimpleTypeConverter->WebConversionService->StringToNumberConverterFactory->StringToNumber
    // RequestParamMethodArgumentResolver->SimpleTypeConverter->WebConversionService->GenericConversionService$NoOpConverter
    @ResponseBody
    @PostMapping("/test4")
    public Result<String> test4(Integer age,
                                MultipartFile file) {
        // req:18 org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile@2c79f164
        logger.info("req:{} {}", age, file);
        return Result.success("");
    }

    // 请求参数:
    // Content-Type: multipart/form-data; boundary=WebAppBoundary
    //
    // --WebAppBoundary
    // Content-Disposition: form-data; name="age"
    //
    // 18
    // --WebAppBoundary
    // Content-Disposition: form-data; name="file"; filename="README.md"
    //
    // < README.md
    // --WebAppBoundary--
    // 简化后的参数处理路径:
    // ModelAttributeMethodProcessor->BeanWrapperImpl->WebConversionService->...(Bean里的属性填充同简单参数处理)
    @ResponseBody
    @PostMapping("/test5")
    public Result<String> test5(TestMultipartParam testMultipartParam) {
        // req:TestMultipartParam{age=18, file=org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile@55f3560}
        logger.info("req:{}", testMultipartParam);
        return Result.success("");
    }
}
