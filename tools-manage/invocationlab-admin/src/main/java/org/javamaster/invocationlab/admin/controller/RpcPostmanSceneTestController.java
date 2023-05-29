package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.dto.AbstractCaseDto;
import org.javamaster.invocationlab.admin.dto.SceneCaseDto;
import org.javamaster.invocationlab.admin.dto.UserCaseDto;
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
import java.util.Set;

/**
 * 场景相关的操作
 * 一个场景用例包含多个用例的请求响应,多个请求依赖前面的响应结果{@link SceneCaseDto}
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
public class RpcPostmanSceneTestController {

    @Autowired
    private RedisRepository cacheService;

    @RequestMapping(value = "case/scene/save", method = RequestMethod.POST)
    @ResponseBody
    public WebApiRspDto<Boolean> saveSceneCase(@RequestBody SceneCaseDto caseDto) {
        String caseName = caseDto.getCaseName();
        String value = JsonUtils.objectToString(caseDto);
        cacheService.mapPut(RedisKeys.SCENE_CASE_KEY, caseName, value);
        return WebApiRspDto.success(Boolean.TRUE);
    }

    @RequestMapping(value = "case/scene/delete", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<Boolean> deleteSceneCase(@RequestParam String caseName) {
        cacheService.removeMap(RedisKeys.SCENE_CASE_KEY, caseName);
        return WebApiRspDto.success(Boolean.TRUE);
    }

    @RequestMapping(value = "case/scene/get", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<SceneCaseDto> getSceneCase(@RequestParam("caseName") String caseName) {
        List<UserCaseDto> testCaseDtoList = new ArrayList<>();
        String value = cacheService.mapGet(RedisKeys.SCENE_CASE_KEY, caseName);
        SceneCaseDto sceneCaseDto = JsonUtils.parseObject(value, SceneCaseDto.class);
        List<UserCaseDto> identifyCaseDtos = sceneCaseDto.getCaseDtoList();
        for (AbstractCaseDto identifyCaseDto : identifyCaseDtos) {
            String jsonStr = cacheService.mapGet(identifyCaseDto.getGroupName(), identifyCaseDto.getCaseName());
            UserCaseDto caseDto = JsonUtils.parseObject(jsonStr, UserCaseDto.class);
            testCaseDtoList.add(caseDto);
        }
        SceneCaseDto rspSceneCaseDto = new SceneCaseDto();
        rspSceneCaseDto.setCaseDtoList(testCaseDtoList);
        rspSceneCaseDto.setSceneScript(sceneCaseDto.getSceneScript());
        return WebApiRspDto.success(rspSceneCaseDto);

    }

    @RequestMapping(value = "case/scene-name/list", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<Set<Object>> getAllSceneName() {
        Set<Object> groupNames = cacheService.mapGetKeys(RedisKeys.SCENE_CASE_KEY);
        return WebApiRspDto.success(groupNames);
    }
}
