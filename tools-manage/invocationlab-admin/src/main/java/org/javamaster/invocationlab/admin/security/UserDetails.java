package org.javamaster.invocationlab.admin.security;

import org.javamaster.invocationlab.admin.security.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author yudong
 */
public class UserDetails extends User implements
        org.springframework.security.core.userdetails.UserDetails {
    private static final long serialVersionUID = 3047964176665864842L;
    private final Collection<GrantedAuthority> authorities;

    public UserDetails(User user) {
        super(user);
        authorities = new HashSet<>();
        if (null != this.getRoles()) {
            this.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.name())));
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
