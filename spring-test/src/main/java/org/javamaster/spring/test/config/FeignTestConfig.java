package org.javamaster.spring.test.config;

import feign.Client;
import feign.Target;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;

import static org.javamaster.spring.test.GeneralTestCode.PROFILE_UNIT_TEST;
import static org.javamaster.spring.test.utils.ReflectTestUtils.reflectGet;
import static org.javamaster.spring.test.utils.ReflectTestUtils.reflectSet;

/**
 * 修改feign service的url以便用于能正常调用
 *
 * @author yudong
 * @date 2021/5/15
 */
@SuppressWarnings("all")
@TestConfiguration
@EnableFeignClients({
        "org.javamaster"
})
@ImportAutoConfiguration({
        RibbonAutoConfiguration.class,
        FeignRibbonClientAutoConfiguration.class,
        FeignAutoConfiguration.class
})
@Profile(PROFILE_UNIT_TEST)
public class FeignTestConfig implements InitializingBean {
    @Autowired
    private ApplicationContext context;
    @Value("${spring.test.feign.services}")
    private String[] services;


    @Override
    public void afterPropertiesSet() throws Exception {
        for (String service : services) {
            String[] split = service.split("\\|");
            changeFeignServiceUrl(split[0], split[1]);
        }
    }

    private void changeFeignServiceUrl(String feignName, String newUrl) throws Exception {
        Object feignService = context.getBean(feignName);
        Object hObj = reflectGet(feignService, "h");

        HashMap<?, ?> dispatchObj = (HashMap<?, ?>) reflectGet(hObj, "dispatch");
        Client client = new Client.Default(null, null);
        for (Object methodHandler : dispatchObj.values()) {
            reflectSet(methodHandler, "client", client);
        }

        Target.HardCodedTarget<?> hardCodedTarget = (Target.HardCodedTarget<?>) reflectGet(hObj, "target");
        System.out.println(feignName);
        System.out.println(hardCodedTarget.name());
        System.out.println(hardCodedTarget.type());
        System.out.println(hardCodedTarget.url());

        reflectSet(hardCodedTarget, "url", newUrl);
        System.out.println("begin----------------------------");
        System.out.println(feignName + " url change to " + newUrl);
        System.out.println("end------------------------------");
    }


}
