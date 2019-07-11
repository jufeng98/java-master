package org.javamaster.b2c.core.service;

import com.github.pagehelper.PageInfo;
import org.javamaster.b2c.core.entity.SysUser;
import org.javamaster.b2c.core.model.vo.ChangeUserStatusReqVo;
import org.javamaster.b2c.core.model.vo.CreateUserReqVo;
import org.javamaster.b2c.core.model.vo.FindUsersReqVo;
import org.javamaster.b2c.core.model.vo.UpdatePasswordReqVo;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Created on 2018/12/9.<br/>
 *
 * @author yudong
 */
public interface IUserService {

    SysUser createUser(CreateUserReqVo reqVo);

    SysUser findUsersByUsername(String username);

    PageInfo<SysUser> findUsers(FindUsersReqVo reqVo);

    Integer changeUserStatus(ChangeUserStatusReqVo reqVo);


    Integer updatePassword(UpdatePasswordReqVo reqVo, UserDetails userDetails);

}
