package org.javamaster.invocationlab.admin.security.user.impl;

import org.javamaster.invocationlab.admin.security.entity.RoleType;
import org.javamaster.invocationlab.admin.security.entity.User;
import org.javamaster.invocationlab.admin.security.user.UserService;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户相关操作
 *
 * @author yudong
 */
@Service
public class UserServiceImpl implements UserService {
    final String defaultAdminCode = "00001";
    @Autowired
    RedisRepository redisRepository;

    @Override
    public List<User> list() {
        List<User> userList = new ArrayList<>();
        List<Object> userStrs = redisRepository.mapGetValues(RedisKeys.USER_KEY);
        for (Object str : userStrs) {
            User user = JsonUtils.parseObject(str.toString(), User.class);
            //只返回有权限的用户
            if (user.getRoles().size() > 0) {
                userList.add(user);
            }
        }
        return userList;
    }

    @Override
    public boolean saveNewUser(User user) {
        String utr = JsonUtils.objectToString(user);
        redisRepository.mapPut(RedisKeys.USER_KEY, user.getUserCode(), utr);
        return true;
    }

    @Override
    public User findOrAdd(String userCode) {
        Object userStr = redisRepository.mapGet(RedisKeys.USER_KEY, userCode);
        if (userStr == null || userStr.toString().isEmpty()) {
            User user = User.of(userCode);
            if (userCode.equals(defaultAdminCode)) {
                user.getRoles().add(RoleType.ADMIN);
            }
            String utr = JsonUtils.objectToString(user);
            redisRepository.mapPut(RedisKeys.USER_KEY, userCode, utr);
            return user;
        } else {
            User user = JsonUtils.parseObject(userStr.toString(), User.class);
            if (userCode.equals(defaultAdminCode)) {
                user.getRoles().add(RoleType.ADMIN);
            }
            return user;
        }
    }

    @Override
    public boolean update(User user) {
        String utr = JsonUtils.objectToString(user);
        redisRepository.mapPut(RedisKeys.USER_KEY, user.getUserCode(), utr);
        return true;
    }

    @Override
    public boolean delete(String userCode) {
        redisRepository.removeMap(RedisKeys.USER_KEY, userCode);
        return true;
    }
}
