package org.javamaster.b2c.core.controller;

import com.github.pagehelper.PageInfo;
import org.javamaster.b2c.core.entity.SysUser;
import org.javamaster.b2c.core.model.Result;
import org.javamaster.b2c.core.model.vo.ChangeUserStatusReqVo;
import org.javamaster.b2c.core.model.vo.CreateUserReqVo;
import org.javamaster.b2c.core.model.vo.FindUsersReqVo;
import org.javamaster.b2c.core.model.vo.UpdatePasswordReqVo;
import org.javamaster.b2c.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理用户信息
 *
 * @author yudong
 * @date 2019/7/5
 */
@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户
     *
     * @param reqVo
     * @return
     */
    @Secured("ROLE_ADMIN")
    @PostMapping("/createUser")
    public Result<SysUser> createUser(@Validated @RequestBody CreateUserReqVo reqVo) {
        return new Result<>(userService.createUser(reqVo));
    }

    /**
     * 启用或者禁用用户
     *
     * @param reqVo
     * @return
     */
    @Secured("ROLE_ADMIN")
    @PostMapping("/changeUserStatus")
    public Result<Integer> changeUserStatus(@RequestBody ChangeUserStatusReqVo reqVo) {
        return new Result<>(userService.changeUserStatus(reqVo));
    }

    /**
     * 拥有管理员权限可查看任何用户信息,否则只能查看自己的信息
     *
     * @param reqVo
     * @return
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #reqVo.sysUser.username == #userDetails.username")
    @PostMapping("/findUsers")
    public Result<List<SysUser>> findUsers(@RequestBody FindUsersReqVo reqVo, @AuthenticationPrincipal UserDetails userDetails) {
        PageInfo<SysUser> pageInfo = userService.findUsers(reqVo);
        return new Result<>(pageInfo.getList(), pageInfo.getTotal());
    }

    /**
     * 拥有管理员权限可修改任何用户的密码,否则只能修改自己的密码
     *
     * @param reqVo
     * @param userDetails
     * @return
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (#reqVo.username == #userDetails.username and !T(org.springframework.util.StringUtils).isEmpty(#reqVo.password))")
    @PostMapping("/updatePassword")
    public Result<Integer> updatePassword(@Validated @RequestBody UpdatePasswordReqVo reqVo,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        return new Result<>(userService.updatePassword(reqVo, userDetails));
    }

}
