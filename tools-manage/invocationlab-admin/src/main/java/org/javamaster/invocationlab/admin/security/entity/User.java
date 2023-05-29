package org.javamaster.invocationlab.admin.security.entity;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yudong
 */
@Data
public class User {
    String userCode;
    Set<RoleType> roles = new HashSet<>();

    public User() {
        super();
    }

    public User(User user) {
        this.userCode = user.userCode;
        this.roles = user.roles;
    }

    public static User of(String userCode) {
        User user = new User();
        user.setUserCode(userCode);
        return user;
    }
}
