package org.javamaster.invocationlab.admin.service;


import org.javamaster.invocationlab.admin.model.kafka.CreateTopicMsgReqVo;
import org.javamaster.invocationlab.admin.model.kafka.GetTopicReqVo;
import org.javamaster.invocationlab.admin.model.kafka.KafkaConnectVo;
import org.javamaster.invocationlab.admin.model.kafka.KafkaTree;
import org.javamaster.invocationlab.admin.model.kafka.SendTopicMsgReqVo;
import org.javamaster.invocationlab.admin.model.kafka.TopicInfo;
import org.javamaster.invocationlab.admin.model.kafka.TopicMsg;

import java.util.List;

/**
 * @author yudong
 */
public interface KafkaService {
    /**
     * 测试连接
     */
    String pingConnect(KafkaConnectVo reqVo) throws Exception;

    /**
     * 保存连接
     */
    String saveConnect(KafkaConnectVo reqVo);

    /**
     * 获取连接列表
     */
    List<KafkaConnectVo> listConnects();

    /**
     * 获取kafka树(包括Brokers, Topics,Consumers)
     */
    List<KafkaTree> getKafkaTrees(String connectId) throws Exception;

    /**
     * 获取topic消息列表
     */
    List<TopicMsg> getTopicMsgList(String connectId, String topic, GetTopicReqVo reqVo) throws Exception;

    /**
     * 获取topic信息列表
     */
    List<TopicInfo> getTopicInfo(String connectId, String topic) throws Exception;

    /**
     * 发送消息
     */
    String sendTopicMsg(SendTopicMsgReqVo reqVo);

    /**
     * 创建topic
     */
    String createTopic(CreateTopicMsgReqVo reqVo);

    /**
     * 删除topic
     */
    String delTopic(String connectId, String topic);

    /**
     * 获取topic订阅者列表
     */
    List<String> getTopicSubscribes(String connectId, String topic);

    /**
     * 获取订阅者订阅的所有topic
     */
    List<String> getSubscribeTopics(String connectId, String groupId);

    /**
     * 清空topic消息
     */
    String clearTopicMsg(String connectId, String topic, Long offset);
}
