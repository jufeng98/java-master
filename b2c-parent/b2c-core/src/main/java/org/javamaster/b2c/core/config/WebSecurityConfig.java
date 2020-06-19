package org.javamaster.b2c.core.config;

import org.javamaster.b2c.core.enums.BizExceptionEnum;
import org.javamaster.b2c.core.exception.BizException;
import org.javamaster.b2c.core.handler.LoginHandler;
import org.javamaster.b2c.core.mapper.ManualSecurityMapper;
import org.javamaster.b2c.core.rememberme.RememberMeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author yudong
 * @date 2019/6/10
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private ManualSecurityMapper manualSecurityMapper;
    @Autowired
    private LoginHandler loginHandler;
    @Autowired
    private RememberMeRepository rememberMeRepository;

    private static final int SECONDS_OF_A_WEEK = 7 * 86400;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .antMatchers("/json/**").authenticated()
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginProcessingUrl("/core/login")
                .successHandler(loginHandler::onAuthenticationSuccess)
                .failureHandler(loginHandler::onAuthenticationFailure)
                .and()
                .rememberMe()
                .rememberMeCookieName("CORE_REMEMBER_ME")
                .rememberMeParameter("coreRememberMe")
                .key("coreKey")
                .tokenValiditySeconds(SECONDS_OF_A_WEEK)
                .tokenRepository(rememberMeRepository)
                .and()
                .logout()
                .clearAuthentication(true)
                .logoutSuccessHandler(loginHandler::onLogoutSuccess)
                .and()
                .csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this::loadUserByUsername).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private UserDetails loadUserByUsername(String username) {
        UserDetails userDetails = manualSecurityMapper.selectUser(username);
        if (userDetails == null) {
            throw new BizException(BizExceptionEnum.INVALID_USER);
        }
        return userDetails;
    }
}

