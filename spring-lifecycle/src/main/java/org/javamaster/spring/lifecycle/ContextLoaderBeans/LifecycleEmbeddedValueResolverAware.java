package org.javamaster.spring.lifecycle.ContextLoaderBeans;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;

/**
 * 扩展点, 实现该接口能够获取Spring EL解析器, 我们的自定义注解需要支持 SPEL 表达式的时候可以使用，非常方便
 *
 * @author yudong
 * @date 2020/4/1
 */
@Slf4j
public class LifecycleEmbeddedValueResolverAware implements EmbeddedValueResolverAware {

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        log.info("setEmbeddedValueResolver invoke:{}", resolver.getClass().getName());
    }
}
