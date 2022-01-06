package org.javamaster.spring.swagger.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.javamaster.spring.swagger.anno.ApiEnum;
import org.javamaster.spring.swagger.enums.SexEnum;

import java.util.Date;

/**
 * @author yudong
 * @date 2022/1/4
 */
@Data
@ApiModel
public class User {
    @ApiModelProperty(value = "用户id", example = "10000001")
    private String userId;
    @ApiModelProperty(value = "用户名", example = "admin")
    private String username;
    @ApiModelProperty(value = "用户描述", example = "超级管理员")
    private String desc;
    @ApiModelProperty(value = "编号", example = "10001")
    private Long id;
    @ApiModelProperty(value = "出生日期", example = "1641394999292")
    private Date birthday;
    @ApiEnum(SexEnum.class)
    @ApiModelProperty(value = "性别", example = "1")
    private Integer sex;
}
