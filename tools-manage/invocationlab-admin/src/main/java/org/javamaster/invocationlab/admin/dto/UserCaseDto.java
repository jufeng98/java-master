package org.javamaster.invocationlab.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用例详细信息
 *
 * @author yudong
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserCaseDto extends AbstractCaseDto {
    String zkAddress;
    String serviceName;
    /**
     * 类路径,包含package和版本
     */
    String interfaceKey;
    /**
     * 接口的简单名称,eg:QueryProvider
     */
    String className;
    String methodName;
    String requestValue;
    String responseValue;
    String testScript;
}
