package org.javamaster.spring.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import static org.javamaster.spring.test.GeneralTestCode.PROFILE_UNIT_TEST;

/**
 * @author yudong
 * @date 2021/5/15
 */
@TestConfiguration
@PropertySource(value = "classpath:application-${env:dev}.properties")
@Profile(PROFILE_UNIT_TEST)
public class PropertyTestConfig {

}
