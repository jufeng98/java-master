package org.javamaster.invocationlab.admin.security;

import org.javamaster.invocationlab.admin.security.entity.User;
import org.javamaster.invocationlab.admin.security.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


/**
 * cas authentication UserDetailsService
 *
 * @author yudong
 */
@Component
public class UserDetailsService implements
        org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    UserService userService;

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user;
        try {
            user = userService.findOrAdd(email);
            if (user == null) {
                throw new UsernameNotFoundException(email);
            }
        } catch (Exception exp) {
            throw new UsernameNotFoundException(email);
        }
        return new UserDetails(user);
    }
}
