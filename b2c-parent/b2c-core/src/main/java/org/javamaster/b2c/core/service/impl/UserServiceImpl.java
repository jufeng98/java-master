package org.javamaster.b2c.core.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.javamaster.b2c.core.entity.SysUser;
import org.javamaster.b2c.core.entity.example.SysUserExample;
import org.javamaster.b2c.core.enums.BizExceptionEnum;
import org.javamaster.b2c.core.exception.BizException;
import org.javamaster.b2c.core.mapper.SysUserMapper;
import org.javamaster.b2c.core.model.Page;
import org.javamaster.b2c.core.model.vo.ChangeUserStatusReqVo;
import org.javamaster.b2c.core.model.vo.CreateUserReqVo;
import org.javamaster.b2c.core.model.vo.FindUsersReqVo;
import org.javamaster.b2c.core.model.vo.UpdatePasswordReqVo;
import org.javamaster.b2c.core.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created on 2018/12/9.<br/>
 *
 * @author yudong
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SysUser findUsersByUsername(String username) {
        return userMapper.selectByPrimaryKey(username);
    }

    @Override
    public PageInfo<SysUser> findUsers(FindUsersReqVo reqVo) {
        Page page = reqVo.getPage();
        PageHelper.startPage(page.getPageNum(), page.getPageSize(), page.getOrderBy());
        SysUserExample sysUserExample = new SysUserExample();
        SysUserExample.Criteria criteria = sysUserExample.createCriteria();
        if (!StringUtils.isEmpty(reqVo.getSysUser().getUsername())) {
            criteria.andUsernameEqualTo(reqVo.getSysUser().getUsername());
        }
        if (reqVo.getSysUser().getEnabled() != null) {
            criteria.andEnabledEqualTo(reqVo.getSysUser().getEnabled());
        }
        List<SysUser> list = userMapper.selectByExample(sysUserExample);
        PageInfo<SysUser> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public Integer changeUserStatus(ChangeUserStatusReqVo reqVo) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(reqVo, user);
        return userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public SysUser createUser(CreateUserReqVo reqVo) {
        if (findUsersByUsername(reqVo.getUsername()) != null) {
            throw new BizException(BizExceptionEnum.USER_EXISTS);
        }
        SysUser user = new SysUser();
        BeanUtils.copyProperties(reqVo, user);
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return userMapper.selectByPrimaryKey(reqVo.getUsername());
    }

    @Override
    public Integer updatePassword(UpdatePasswordReqVo reqVo, UserDetails userDetails) {
        SysUser dbUsers = findUsersByUsername(reqVo.getUsername());
        if (dbUsers == null) {
            throw new BizException(BizExceptionEnum.USERNAME_NOT_EXISTS);
        }
        // 非管理员需要验证原密码
        if (!StringUtils.isEmpty(reqVo.getPassword()) &&
                !passwordEncoder.matches(reqVo.getPassword(), dbUsers.getPassword())) {
            throw new BizException(BizExceptionEnum.PASSWORD_INCORRECT);
        }
        dbUsers.setPassword(passwordEncoder.encode(reqVo.getNewPassword()));
        int affectRows = userMapper.updateByPrimaryKey(dbUsers);
        return affectRows;
    }

}
