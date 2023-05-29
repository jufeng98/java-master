package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.dto.UserCaseDto;
import org.javamaster.invocationlab.admin.dto.UserCaseGroupDto;
import org.javamaster.invocationlab.admin.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 用例相关的操作
 * 用例在这个系统里面指一个接口的请求,目前来说是对一个dubbo接口的请求{@link UserCaseDto}
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
public class RpcPostmanTestCaseController extends AbstractController {

    @Autowired
    private RedisRepository cacheService;

    @RequestMapping(value = "case/save", method = RequestMethod.POST)
    @ResponseBody
    public WebApiRspDto<Boolean> saveCase(@RequestBody UserCaseDto caseDto) {
        String groupName = caseDto.getGroupName();
        cacheService.setAdd(RedisKeys.CASE_KEY, groupName);
        String value = JsonUtils.objectToString(caseDto);
        cacheService.mapPut(groupName, caseDto.getCaseName(), value);
        return WebApiRspDto.success(Boolean.TRUE);
    }

    @RequestMapping(value = "case/group-case-detail/list", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<List<UserCaseDto>> getAllGroupCaseDetail() {
        List<UserCaseDto> groupDtoList = new ArrayList<>(1);
        Set<Object> groupNames = cacheService.members(RedisKeys.CASE_KEY);
        for (Object obj : groupNames) {
            Set<Object> caseNames = cacheService.mapGetKeys((String) obj);
            for (Object sub : caseNames) {
                String jsonStr = cacheService.mapGet(obj.toString(), sub.toString());
                UserCaseDto caseDto = JsonUtils.parseObject(jsonStr, UserCaseDto.class);
                groupDtoList.add(caseDto);
            }
        }
        return WebApiRspDto.success(groupDtoList);
    }

    @RequestMapping(value = "case/group/list", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<List<UserCaseGroupDto>> getAllGroupAndCaseName() {
        List<UserCaseGroupDto> groupDtoList = new ArrayList<>(1);
        Set<Object> groupNames = cacheService.members(RedisKeys.CASE_KEY);
        for (Object obj : groupNames) {
            UserCaseGroupDto parentDto = new UserCaseGroupDto();
            parentDto.setValue(obj.toString());
            parentDto.setLabel(obj.toString());
            Set<Object> caseNames = cacheService.mapGetKeys((String) obj);
            List<UserCaseGroupDto> children = new ArrayList<>(1);
            parentDto.setChildren(children);
            for (Object sub : caseNames) {
                UserCaseGroupDto dto = new UserCaseGroupDto();
                dto.setValue(sub.toString());
                dto.setLabel(sub.toString());
                dto.setChildren(null);
                children.add(dto);
            }
            groupDtoList.add(parentDto);
        }
        return WebApiRspDto.success(groupDtoList);
    }

    @RequestMapping(value = "case/group-name/list", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<List<String>> getAllGroupName() {
        List<String> groupDtoList = new ArrayList<>(1);
        Set<Object> groupNames = cacheService.members(RedisKeys.CASE_KEY);
        for (Object obj : groupNames) {
            UserCaseGroupDto groupDto = new UserCaseGroupDto();
            groupDto.setValue(obj.toString());
            groupDto.setLabel(obj.toString());
            groupDto.setChildren(null);
            groupDtoList.add(groupDto.getValue());
        }
        return WebApiRspDto.success(groupDtoList);
    }

    @RequestMapping(value = "case/detail", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<UserCaseDto> queryCaseDetail(@RequestParam(value = "groupName") String groupName,
                                                     @RequestParam(value = "caseName") String caseName) {
        String jsonStr = cacheService.mapGet(groupName, caseName);
        UserCaseDto caseDto = JsonUtils.parseObject(jsonStr, UserCaseDto.class);
        if (Objects.requireNonNull(caseDto).getClassName() == null) {
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
        cacheService.removeMap(groupName, caseName);
        return WebApiRspDto.success("删除成功");
    }
}
