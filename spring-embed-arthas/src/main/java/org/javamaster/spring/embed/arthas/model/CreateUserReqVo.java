package org.javamaster.spring.embed.arthas.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author yudong
 * @date 2022/6/4
 */
@Data
public class CreateUserReqVo {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String email;
}
