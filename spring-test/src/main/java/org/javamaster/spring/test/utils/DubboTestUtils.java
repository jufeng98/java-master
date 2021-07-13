package org.javamaster.spring.test.utils;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.javamaster.spring.test.utils.ReflectTestUtils.reflectGet;
import static org.javamaster.spring.test.utils.ReflectTestUtils.reflectSet;

/**
 * @author yudong
 * @date 2021/5/15
 */
public class DubboTestUtils {

    @SneakyThrows
    public static void changeDubboReferenceProperty(ApplicationContext context, String referenceName,
                                                    String propertyName, String propertyValue) {
        ReferenceAnnotationBeanPostProcessor processor = (ReferenceAnnotationBeanPostProcessor) context
                .getBean("referenceAnnotationBeanPostProcessor");

        Field injectedObjectsCacheField = ReflectionUtils
                .findField(ReferenceAnnotationBeanPostProcessor.class, "injectedObjectsCache");
        ReflectionUtils.makeAccessible(Objects.requireNonNull(injectedObjectsCacheField));
        ConcurrentHashMap<?, ?> map = (ConcurrentHashMap<?, ?>) injectedObjectsCacheField.get(processor);

        for (Object key : map.keySet()) {
            if (key.toString().contains(referenceName)) {
                Proxy proxy = (Proxy) map.get(key);

                Object hObj = reflectGet(proxy, "h");

                ReferenceBean<?> oldReferenceBean = (ReferenceBean<?>) reflectGet(hObj, "referenceBean");

                ReferenceBean<?> newReferenceBean = new ReferenceBean<>();
                BeanUtils.copyProperties(oldReferenceBean, newReferenceBean);
                newReferenceBean.setCheck(false);

                reflectSet(newReferenceBean, propertyName, propertyValue);

                reflectSet(hObj, "referenceBean", newReferenceBean);

                reflectSet(hObj, "bean", newReferenceBean.get());
                System.out.println("begin----------------------------");
                System.out.println(key);
                System.out.println(referenceName + " reference " + propertyName + " change to " + propertyValue);
                System.out.println("end------------------------------");
            }
        }
    }

}
