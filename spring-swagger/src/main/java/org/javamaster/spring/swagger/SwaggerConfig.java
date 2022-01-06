package org.javamaster.spring.swagger;

import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 * @date 2022/1/4
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket docket() {
        List<RequestParameter> params = new ArrayList<>();
        RequestParameter param = new RequestParameterBuilder()
                // 全局header参数
                .name("Authorization")
                .description("访问授权码头字段")
                .required(true)
                .in(ParameterType.HEADER)
                .build();
        params.add(param);
        return new Docket(DocumentationType.SWAGGER_2)
                // 启用swagger
                .enable(true)
                .select()
                // Controller类需要有Api注解才能生成接口文档
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                // Controller方法需要有ApiOperation注解才能生成接口文档
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                // 路径使用any风格
                .paths(PathSelectors.any())
                .build()
                .globalRequestParameters(params)
                .consumes(Sets.newHashSet(MediaType.APPLICATION_JSON_VALUE))
                .produces(Sets.newHashSet(MediaType.APPLICATION_JSON_VALUE))
                // 接口文档的基本信息
                .apiInfo(apiInfo());
    }

    @SneakyThrows
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("业务中台")
                .description("这是项目描述")
                .contact(new Contact("liangyudong", "https://www.zhihu.com/people/liang-yu-dong-44/posts",
                        "375709770@qq.com"))
                .version("1.0.0")
                .build();
    }
}
