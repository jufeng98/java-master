package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.annos.AopLog;
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.model.kafka.CreateTopicMsgReqVo;
import org.javamaster.invocationlab.admin.model.kafka.GetTopicReqVo;
import org.javamaster.invocationlab.admin.model.kafka.KafkaConnectVo;
import org.javamaster.invocationlab.admin.model.kafka.KafkaTree;
import org.javamaster.invocationlab.admin.model.kafka.SendTopicMsgReqVo;
import org.javamaster.invocationlab.admin.model.kafka.TopicInfo;
import org.javamaster.invocationlab.admin.model.kafka.TopicMsg;
import org.javamaster.invocationlab.admin.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yudong
 */
@RestController
@RequestMapping("/kafka")
public class KafkaController {
    @Autowired
    private KafkaService kafkaService;

    @PostMapping("/pingConnect")
    public WebApiRspDto<String> pingConnect(@RequestBody KafkaConnectVo reqVo) throws Exception {
        return WebApiRspDto.success(kafkaService.pingConnect(reqVo));
    }

    @AopLog
    @PostMapping("/saveConnect")
    public WebApiRspDto<String> saveConnect(@RequestBody KafkaConnectVo reqVo) {
        return WebApiRspDto.success(kafkaService.saveConnect(reqVo));
    }

    @PostMapping("/listConnects")
    public WebApiRspDto<List<KafkaConnectVo>> listConnects() {
        return WebApiRspDto.success(kafkaService.listConnects());
    }

    @AopLog
    @PostMapping("/sendTopicMsg/{connectId}")
    public WebApiRspDto<String> sendTopicMsg(@PathVariable String connectId, @RequestBody SendTopicMsgReqVo reqVo) {
        reqVo.setConnectId(connectId);
        return WebApiRspDto.success(kafkaService.sendTopicMsg(reqVo));
    }

    @GetMapping("/getKafkaTrees/{connectId}")
    public WebApiRspDto<List<KafkaTree>> getKafkaTrees(@PathVariable String connectId) throws Exception {
        return WebApiRspDto.success(kafkaService.getKafkaTrees(connectId));
    }

    @GetMapping("/getTopicInfo/{connectId}/{topic}")
    public WebApiRspDto<List<TopicInfo>> getTopicInfo(@PathVariable String connectId, @PathVariable String topic) throws Exception {
        return WebApiRspDto.success(kafkaService.getTopicInfo(connectId, topic));
    }

    @AopLog
    @PostMapping("/clearTopicMsg/{connectId}/{topic}")
    public WebApiRspDto<String> clearTopicMsg(@PathVariable String connectId, @PathVariable String topic,
                                              @RequestParam(required = false) Long offset) {
        return WebApiRspDto.success(kafkaService.clearTopicMsg(connectId, topic, offset));
    }

    @GetMapping("/getTopicSubscribes/{connectId}/{topic}")
    public WebApiRspDto<List<String>> getTopicSubscribes(@PathVariable String connectId, @PathVariable String topic) {
        return WebApiRspDto.success(kafkaService.getTopicSubscribes(connectId, topic));
    }

    @GetMapping("/getSubscribeTopics/{connectId}/{groupId}")
    public WebApiRspDto<List<String>> getSubscribeTopics(@PathVariable String connectId, @PathVariable String groupId) {
        return WebApiRspDto.success(kafkaService.getSubscribeTopics(connectId, groupId));
    }

    @PostMapping("/getTopicMsgList/{connectId}/{topic}")
    public WebApiRspDto<List<TopicMsg>> getTopicMsgList(@PathVariable String connectId, @PathVariable String topic,
                                                        @RequestBody GetTopicReqVo reqVo) throws Exception {
        return WebApiRspDto.success(kafkaService.getTopicMsgList(connectId, topic, reqVo));
    }

    @AopLog
    @PostMapping("/createTopic/{connectId}")
    public WebApiRspDto<String> createTopic(@PathVariable String connectId, @RequestBody CreateTopicMsgReqVo reqVo) {
        reqVo.setConnectId(connectId);
        return WebApiRspDto.success(kafkaService.createTopic(reqVo));
    }

    @AopLog
    @PostMapping("/delTopic/{connectId}/{topic}")
    public WebApiRspDto<String> delTopic(@PathVariable String connectId, @PathVariable String topic) {
        return WebApiRspDto.success(kafkaService.delTopic(connectId, topic));
    }
}
