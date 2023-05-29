package org.javamaster.invocationlab.admin.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试场景详细信息标识
 *
 * @author yudong
 */
@Data
public class SceneCaseDto {
    String caseName;
    List<UserCaseDto> caseDtoList = new ArrayList<>();
    String sceneScript;
}
