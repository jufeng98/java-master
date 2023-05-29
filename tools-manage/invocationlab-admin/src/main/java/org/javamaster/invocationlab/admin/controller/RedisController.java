package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.model.redis.CommonVo;
import org.javamaster.invocationlab.admin.model.redis.ConnectionVo;
import org.javamaster.invocationlab.admin.model.redis.Tree;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yudong
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
    public WebApiRspDto<ValueVo> getValue(@RequestBody CommonVo commonVo) throws Exception {
        return WebApiRspDto.success(redisService.getValue(commonVo));
    }

    @RequestMapping(value = "/saveValue", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<ValueVo> saveValue(@RequestBody CommonVo commonVo) throws Exception {
        return WebApiRspDto.success(redisService.saveValue(commonVo));
    }

    @RequestMapping(value = "/setNewTtl", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<String> setNewTtl(@RequestBody CommonVo commonVo) throws Exception {
        return WebApiRspDto.success(redisService.setNewTtl(commonVo));
    }

    @RequestMapping(value = "/addKey", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<ValueVo> addKey(@RequestBody CommonVo commonVo) throws Exception {
        return WebApiRspDto.success(redisService.addKey(commonVo));
    }

    @RequestMapping(value = "/delKey", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<String> delKey(@RequestBody CommonVo commonVo) throws Exception {
        return WebApiRspDto.success(redisService.delKey(commonVo));
    }

    @RequestMapping(value = "/delField", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<String> delField(@RequestBody CommonVo commonVo) throws Exception {
        return WebApiRspDto.success(redisService.delField(commonVo));
    }

    @RequestMapping(value = "/addField", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<String> addField(@RequestBody CommonVo commonVo) throws Exception {
        return WebApiRspDto.success(redisService.addField(commonVo));
    }

    @RequestMapping(value = "/renameKey", method = {RequestMethod.GET, RequestMethod.POST})
    public WebApiRspDto<ValueVo> renameKey(@RequestBody CommonVo commonVo) throws Exception {
        return WebApiRspDto.success(redisService.renameKey(commonVo));
    }
}
