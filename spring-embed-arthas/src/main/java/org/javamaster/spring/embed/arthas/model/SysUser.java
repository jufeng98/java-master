package org.javamaster.spring.embed.arthas.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yudong
 * @date 2022/6/4
 */
@Data
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1821414069229130752L;
    private String username;
    private String password;
    private String email;
}