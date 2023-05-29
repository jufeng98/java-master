package org.javamaster.invocationlab.admin.security.user;


import org.javamaster.invocationlab.admin.security.entity.User;

import java.util.List;

/**
 * @author yudong
 */
public interface UserService {

    List<User> list();

    boolean saveNewUser(User user);

    User findOrAdd(String userCode);

    boolean update(User user);

    boolean delete(String userCode);
}
