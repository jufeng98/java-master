package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.ConnectionVo;
import org.javamaster.invocationlab.admin.model.redis.Tree;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.service.RedisService;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author yudong
 * @date 2023/3/3
 */
@RestController
@RequestMapping("/redis")
public class RedisController {
    @Autowired
    private RedisService redisService;

    @RequestMapping(value = "/pingConnect", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<String> pingConnect(@RequestBody ConnectionVo connectionVoReq) throws Exception {
        return WebApiRspDto.success(redisService.pingConnect(connectionVoReq));
    }

    @RequestMapping(value = "/saveConnect", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<String> saveConnect(@RequestBody ConnectionVo connectionVoReq) throws Exception {
        return WebApiRspDto.success(redisService.saveConnect(connectionVoReq));
    }

    @RequestMapping(value = "/listConnects", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<List<ConnectionVo>> listConnects() throws Exception {
        return WebApiRspDto.success(redisService.listConnects());
    }

    @RequestMapping(value = "/listDb/{connectId}", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<List<Tree>> listDb(@PathVariable String connectId) throws Exception {
        return WebApiRspDto.success(redisService.listDb(connectId));
    }

    @RequestMapping(value = "/listKeys/{connectId}/{redisDbIndex}", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<List<Tree>> listKeys(@PathVariable String connectId, @PathVariable Integer redisDbIndex,
                                             @RequestParam String pattern) throws Exception {
        return WebApiRspDto.success(redisService.listKeys(connectId, redisDbIndex, pattern));
    }

    @RequestMapping(value = "/getValue", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<ValueVo> getValue(@RequestBody CommonRedisVo commonRedisVo) throws Exception {
        return WebApiRspDto.success(redisService.getValue(commonRedisVo));
    }

    @RequestMapping(value = "/saveValue", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<ValueVo> saveValue(@RequestBody CommonRedisVo commonRedisVo) throws Exception {
        return WebApiRspDto.success(redisService.saveValue(commonRedisVo));
    }

    @RequestMapping(value = "/setNewTtl", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<String> setNewTtl(@RequestBody CommonRedisVo commonRedisVo) throws Exception {
        return WebApiRspDto.success(redisService.setNewTtl(commonRedisVo));
    }

    @RequestMapping(value = "/addKey", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<ValueVo> addKey(@RequestBody CommonRedisVo commonRedisVo) throws Exception {
        return WebApiRspDto.success(redisService.addKey(commonRedisVo));
    }

    @RequestMapping(value = "/delKey", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<String> delKey(@RequestBody CommonRedisVo commonRedisVo) throws Exception {
        return WebApiRspDto.success(redisService.delKey(commonRedisVo));
    }

    @RequestMapping(value = "/renameKey", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<ValueVo> renameKey(@RequestBody CommonRedisVo commonRedisVo) throws Exception {
        return WebApiRspDto.success(redisService.renameKey(commonRedisVo));
    }

    @RequestMapping(value = "/delField", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<String> delField(@RequestBody CommonRedisVo commonRedisVo) throws Exception {
        return WebApiRspDto.success(redisService.delField(commonRedisVo));
    }

    @RequestMapping(value = "/addField", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<String> addField(@RequestBody CommonRedisVo commonRedisVo) throws Exception {
        return WebApiRspDto.success(redisService.addField(commonRedisVo));
    }

    @RequestMapping(value = "/actuator/health", method = {RequestMethod.GET})
    public String health() {
        return Arrays.toString(SpringUtils.getContext().getEnvironment().getActiveProfiles());
    }
}
