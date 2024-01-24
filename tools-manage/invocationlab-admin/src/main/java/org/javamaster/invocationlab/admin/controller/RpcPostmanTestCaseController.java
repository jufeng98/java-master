package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.model.dto.UserCaseDto;
import org.javamaster.invocationlab.admin.model.dto.UserCaseGroupDto;
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用例相关的操作
 * 用例在这个系统里面指一个接口的请求,目前来说是对一个dubbo接口的请求{@link UserCaseDto}
 *
 * @author yudong
 */
@SuppressWarnings("VulnerableCodeUsages")
@Controller
@RequestMapping("/dubbo-postman/")
public class RpcPostmanTestCaseController extends AbstractController {

    @Autowired
    private RedisRepository redisRepository;

    @RequestMapping(value = "case/save", method = RequestMethod.POST)
    @ResponseBody
    @SneakyThrows
    public WebApiRspDto<Boolean> saveCase(@RequestBody UserCaseDto caseDto) {
        String groupName = caseDto.getGroupName();
        redisRepository.setAdd(RedisKeys.CASE_KEY, groupName);

        String value = JsonUtils.jacksonObjectMapper.writeValueAsString(caseDto);
        redisRepository.mapPut(RedisKeys.CASE_KEY_SUB + "_" + groupName, caseDto.getCaseName(), value);

        return WebApiRspDto.success(Boolean.TRUE);
    }

    @RequestMapping(value = "case/group-case-detail/list", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<List<UserCaseDto>> getAllGroupCaseDetail() {
        Set<String> groupNames = redisRepository.members(RedisKeys.CASE_KEY);
        List<UserCaseDto> groupDtoList = groupNames.stream()
                .map(groupName -> {
                    Set<String> caseNames = redisRepository.mapGetKeys(RedisKeys.CASE_KEY_SUB + "_" + groupName);
                    return caseNames.stream()
                            .map(caseName -> {
                                String jsonStr = redisRepository.mapGet(RedisKeys.CASE_KEY_SUB + "_" + groupName, caseName);
                                try {
                                    return (UserCaseDto) JsonUtils.jacksonObjectMapper.readValue(jsonStr, Object.class);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.toList());
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return WebApiRspDto.success(groupDtoList);
    }

    @RequestMapping(value = "case/group/list", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<List<UserCaseGroupDto>> getAllGroupAndCaseName() {
        Set<String> groupNames = redisRepository.members(RedisKeys.CASE_KEY);

        List<UserCaseGroupDto> groupDtoList = groupNames.stream()
                .map(groupName -> {
                    UserCaseGroupDto parentDto = new UserCaseGroupDto();
                    parentDto.setValue(groupName);
                    parentDto.setLabel(groupName);

                    Set<String> caseNames = redisRepository.mapGetKeys(RedisKeys.CASE_KEY_SUB + "_" + groupName);

                    List<UserCaseGroupDto> children = caseNames.stream()
                            .map(caseName -> {
                                UserCaseGroupDto dto = new UserCaseGroupDto();
                                dto.setValue(caseName);
                                dto.setLabel(caseName);
                                dto.setChildren(null);
                                return dto;
                            })
                            .collect(Collectors.toList());

                    parentDto.setChildren(children);

                    return parentDto;
                })
                .sorted((g1, g2) -> g1.getLabel().compareToIgnoreCase(g2.getLabel()))
                .collect(Collectors.toList());
        return WebApiRspDto.success(groupDtoList);
    }

    @RequestMapping(value = "case/group-name/list", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<List<String>> getAllGroupName() {
        Set<Object> groupNames = redisRepository.members(RedisKeys.CASE_KEY);

        List<String> groupDtoList = groupNames.stream()
                .map(obj -> {
                    UserCaseGroupDto groupDto = new UserCaseGroupDto();
                    groupDto.setValue(obj.toString());
                    groupDto.setLabel(obj.toString());
                    groupDto.setChildren(null);
                    return groupDto.getValue();
                })
                .collect(Collectors.toList());
        return WebApiRspDto.success(groupDtoList);
    }

    @RequestMapping(value = "case/detail", method = RequestMethod.GET)
    @ResponseBody
    @SneakyThrows
    public WebApiRspDto<UserCaseDto> queryCaseDetail(@RequestParam(value = "groupName") String groupName,
                                                     @RequestParam(value = "caseName") String caseName) {
        String jsonStr = redisRepository.mapGet(RedisKeys.CASE_KEY_SUB + "_" + groupName, caseName);
        UserCaseDto caseDto = (UserCaseDto) JsonUtils.jacksonObjectMapper.readValue(jsonStr, Object.class);

        if (caseDto.getClassName() == null) {
            Map<String, String> classNameMap = getAllSimpleClassName(caseDto.getZkAddress(), caseDto.getServiceName());
            for (Map.Entry<String, String> item : classNameMap.entrySet()) {
                if (item.getValue().equals(caseDto.getInterfaceKey())) {
                    caseDto.setClassName(item.getKey());
                    break;
                }
            }
        }

        return WebApiRspDto.success(caseDto);
    }

    @RequestMapping(value = "case/delete", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<String> deleteDetail(@RequestParam(value = "groupName") String groupName,
                                             @RequestParam(value = "caseName") String caseName) {
        redisRepository.removeMap(groupName, caseName);
        Set<String> caseNames = redisRepository.mapGetKeys(groupName);

        if (CollectionUtils.isEmpty(caseNames)) {
            redisRepository.delete(RedisKeys.CASE_KEY_SUB + "_" + groupName);
            redisRepository.setRemove(RedisKeys.CASE_KEY, groupName);
        }

        return WebApiRspDto.success("删除成功");
    }
}
