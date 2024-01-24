package org.javamaster.invocationlab.admin.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用例分组
 *
 * @author yudong
 */
@Data
public class UserCaseGroupDto {
    String value;
    String label;
    List<UserCaseGroupDto> children = new ArrayList<>();
}
