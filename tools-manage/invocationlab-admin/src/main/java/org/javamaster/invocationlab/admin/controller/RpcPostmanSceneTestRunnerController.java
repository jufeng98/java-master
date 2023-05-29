package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.dto.AbstractCaseDto;
import org.javamaster.invocationlab.admin.dto.SceneCaseDto;
import org.javamaster.invocationlab.admin.dto.UserCaseDto;
import org.javamaster.invocationlab.admin.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.service.scenetest.SceneTester;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 批量处理用例
 * 场景测试运行相关的操作
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
public class RpcPostmanSceneTestRunnerController {
    @Autowired
    RedisRepository cacheService;
    @Autowired
    SceneTester requestService;

    @ResponseBody
    @RequestMapping(value = "case/scene/run", method = RequestMethod.POST)
    public WebApiRspDto<Map<String, Object>> runSceneCase(@RequestBody SceneCaseDto sceneDto) {
        List<UserCaseDto> testCaseDtoList = new ArrayList<>(1);
        for (AbstractCaseDto dto : sceneDto.getCaseDtoList()) {
            String jsonStr = cacheService.mapGet(dto.getGroupName(), dto.getCaseName());
            UserCaseDto caseDto = JsonUtils.parseObject(jsonStr, UserCaseDto.class);
            testCaseDtoList.add(caseDto);
        }
        Map<String, Object> rst = requestService.process(testCaseDtoList, sceneDto.getSceneScript());
        return WebApiRspDto.success(rst);
    }
}
