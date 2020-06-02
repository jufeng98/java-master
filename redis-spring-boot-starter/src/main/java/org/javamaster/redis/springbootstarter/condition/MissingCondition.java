package org.javamaster.redis.springbootstarter.condition;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author yudong
 * @date 2020/5/18
 */
public class MissingCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            context.getBeanFactory().getBean(RedisTemplate.class);
            return false;
        } catch (NoSuchBeanDefinitionException e) {
            return true;
        }
    }

}
