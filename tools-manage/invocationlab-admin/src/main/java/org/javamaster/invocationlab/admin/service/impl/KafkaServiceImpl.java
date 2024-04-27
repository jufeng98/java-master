package org.javamaster.invocationlab.admin.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteRecordsOptions;
import org.apache.kafka.clients.admin.DeleteRecordsResult;
import org.apache.kafka.clients.admin.DeleteTopicsOptions;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DeletedRecords;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsOptions;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupsOptions;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.MemberAssignment;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.model.kafka.CreateTopicMsgReqVo;
import org.javamaster.invocationlab.admin.model.kafka.GetTopicReqVo;
import org.javamaster.invocationlab.admin.model.kafka.KafkaConnectVo;
import org.javamaster.invocationlab.admin.model.kafka.KafkaTree;
import org.javamaster.invocationlab.admin.model.kafka.SendTopicMsgReqVo;
import org.javamaster.invocationlab.admin.model.kafka.TopicInfo;
import org.javamaster.invocationlab.admin.model.kafka.TopicMsg;
import org.javamaster.invocationlab.admin.service.KafkaService;
import org.javamaster.invocationlab.admin.util.KafkaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yudong
 */
@Service
@Slf4j
public class KafkaServiceImpl implements KafkaService {
    public static final String HASH_KEY_KAFKA = "admin:erd:kafka:cluster";
    @Autowired
    private RedisTemplate<String, Object> redisTemplateJackson;
    @Value("${kafka.admin.url}")
    private String kafkaAdminUrl;
    @Autowired
    private RestTemplate restTemplate;
    public static final int TIME_OUT_MS = 6000;
    public static final Duration TIME_OUT_DURATION = Duration.ofMillis(TIME_OUT_MS);

    @Override
    public String pingConnect(KafkaConnectVo connectVo) throws Exception {
        Map<String, Object> prop = Maps.newHashMap();
        prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, connectVo.getNodes());
        try (KafkaAdminClient kafkaAdminClient = (KafkaAdminClient) KafkaAdminClient.create(prop)) {
            DescribeClusterOptions describeClusterOptions = new DescribeClusterOptions();
            describeClusterOptions.timeoutMs(TIME_OUT_MS);
            DescribeClusterResult describeClusterResult = kafkaAdminClient.describeCluster(describeClusterOptions);
            Collection<Node> nodes = describeClusterResult.nodes().get(TIME_OUT_MS, TimeUnit.MILLISECONDS);
            log.info("连接成功:{}", nodes);
        } catch (TimeoutException e) {
            log.error("连接失败", e);
            throw new BizException(connectVo.getNodes() + "连接失败");
        }
        return connectVo.getNodes() + "连接成功";
    }

    @Override
    public String saveConnect(KafkaConnectVo connectVo) {
        String connectId = RandomStringUtils.randomAlphabetic(12);
        connectVo.setConnectId(connectId);
        connectVo.setCreateTime(new Date());
        redisTemplateJackson.opsForHash().put(HASH_KEY_KAFKA, connectId, connectVo);
        return "保存成功";
    }

    @Override
    public List<KafkaConnectVo> listConnects() {
        List<Object> values = redisTemplateJackson.opsForHash().values(HASH_KEY_KAFKA);
        return values.stream()
                .map(obj -> (KafkaConnectVo) obj)
                .sorted(Comparator.comparing(KafkaConnectVo::getCreateTime))
                .collect(Collectors.toList());
    }

    private <T> T execAdminClient(String connectId, Function<KafkaAdminClient, T> function) {
        KafkaConnectVo connectVo = (KafkaConnectVo) redisTemplateJackson.opsForHash().get(HASH_KEY_KAFKA, connectId);
        Objects.requireNonNull(connectVo);

        KafkaAdminClient kafkaAdminClient = KafkaUtils.kafkaAdminClientSingleton(connectId, connectVo.getNodes());
        return function.apply(kafkaAdminClient);
    }

    private <T> T execKafkaTemplate(String connectId, Function<KafkaTemplate<Object, Object>, T> function) {
        KafkaConnectVo connectVo = (KafkaConnectVo) redisTemplateJackson.opsForHash().get(HASH_KEY_KAFKA, connectId);
        Objects.requireNonNull(connectVo);

        KafkaTemplate<Object, Object> kafkaTemplate = KafkaUtils.kafkaTemplateSingleton(connectId, connectVo.getNodes());
        return function.apply(kafkaTemplate);
    }

    private <T> T execKafkaConsumer(String connectId, Function<KafkaConsumer<Object, Object>, T> function) {
        KafkaConnectVo connectVo = (KafkaConnectVo) redisTemplateJackson.opsForHash().get(HASH_KEY_KAFKA, connectId);
        Objects.requireNonNull(connectVo);

        KafkaConsumer<Object, Object> kafkaConsumer = KafkaUtils.kafkaConsumerSingleton(connectId, connectVo.getNodes());
        synchronized (KafkaServiceImpl.class) {
            return function.apply(kafkaConsumer);
        }
    }

    @Override
    public String createTopic(CreateTopicMsgReqVo reqVo) {
        return execAdminClient(reqVo.getConnectId(), kafkaAdminClient -> {
            NewTopic newTopic = new NewTopic(reqVo.getTopic(), reqVo.getNumPartitions(), reqVo.getReplicationFactor());
            CreateTopicsOptions options = new CreateTopicsOptions();
            options.timeoutMs(TIME_OUT_MS);
            CreateTopicsResult createTopicsResult = kafkaAdminClient.createTopics(Collections.singletonList(newTopic), options);
            KafkaFuture<Void> all = createTopicsResult.all();
            try {
                all.get();
            } catch (Exception e) {
                log.error("创建topic失败", e);
                if (!tryCreateTopicByKafkaAdminUrl(reqVo)) {
                    throw new BizException("创建topic失败:" + e.getMessage());
                }
            }
            return "创建成功";
        });
    }

    @Override
    public String delTopic(String connectId, String topic) {
        return execAdminClient(connectId, kafkaAdminClient -> {
            DeleteTopicsOptions options = new DeleteTopicsOptions();
            options.timeoutMs(TIME_OUT_MS);
            DeleteTopicsResult deleteTopicsResult = kafkaAdminClient.deleteTopics(Collections.singletonList(topic),
                    options);
            KafkaFuture<Void> all = deleteTopicsResult.all();
            try {
                all.get();
            } catch (Exception e) {
                log.error("删除topic失败", e);
                if (!tryDelTopicByKafkaAdminUrl(topic)) {
                    throw new BizException("删除topic失败:" + e.getMessage());
                }
            }
            return "删除成功";
        });
    }

    @Override
    public List<String> getTopicSubscribes(String connectId, String topic) {
        return execAdminClient(connectId, kafkaAdminClient -> {
            ListConsumerGroupsOptions options = new ListConsumerGroupsOptions();
            options.timeoutMs(TIME_OUT_MS);
            ListConsumerGroupsResult listConsumerGroupsResult = kafkaAdminClient.listConsumerGroups(options);
            Collection<ConsumerGroupListing> consumerGroupListings;
            try {
                consumerGroupListings = listConsumerGroupsResult.valid().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            List<String> groupIds = consumerGroupListings.stream()
                    .map(ConsumerGroupListing::groupId)
                    .collect(Collectors.toList());

            DescribeConsumerGroupsOptions groupsOptions = new DescribeConsumerGroupsOptions();
            groupsOptions.timeoutMs(TIME_OUT_MS);
            DescribeConsumerGroupsResult result = kafkaAdminClient.describeConsumerGroups(groupIds, groupsOptions);
            Map<String, ConsumerGroupDescription> map;
            try {
                map = result.all().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return map.values().stream()
                    .filter(consumerGroupDescription -> {
                        Collection<MemberDescription> members = consumerGroupDescription.members();
                        return members.stream()
                                .anyMatch(it -> {
                                    MemberAssignment assignment = it.assignment();
                                    Set<TopicPartition> topicPartitions = assignment.topicPartitions();
                                    return topicPartitions.stream().anyMatch(innerIt -> innerIt.topic().equals(topic));
                                });
                    })
                    .map(ConsumerGroupDescription::groupId)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<String> getSubscribeTopics(String connectId, String groupId) {
        return execAdminClient(connectId, kafkaAdminClient -> {
            DescribeConsumerGroupsOptions options = new DescribeConsumerGroupsOptions();
            options.timeoutMs(TIME_OUT_MS);
            DescribeConsumerGroupsResult result = kafkaAdminClient.describeConsumerGroups(Lists.newArrayList(groupId), options);
            Map<String, ConsumerGroupDescription> map;
            try {
                map = result.all().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return map.values().stream()
                    .map(consumerGroupDescription -> {
                        Collection<MemberDescription> members = consumerGroupDescription.members();
                        return members.stream()
                                .map(it -> {
                                    MemberAssignment assignment = it.assignment();
                                    Set<TopicPartition> topicPartitions = assignment.topicPartitions();
                                    return topicPartitions.stream()
                                            .map(TopicPartition::topic)
                                            .collect(Collectors.toList());
                                })
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
                    })
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        });
    }

    @Override
    public String clearTopicMsg(String connectId, String topic, Long offset) {
        return execAdminClient(connectId, kafkaAdminClient ->
                execKafkaConsumer(connectId, kafkaConsumer -> {
                    List<PartitionInfo> partitionInfoList = kafkaConsumer.partitionsFor(topic, TIME_OUT_DURATION);
                    Collection<TopicPartition> partitions = partitionInfoList.stream()
                            .map(it -> new TopicPartition(topic, it.partition()))
                            .collect(Collectors.toList());

                    Map<TopicPartition, RecordsToDelete> recordsToDeleteMap = Maps.newHashMap();
                    for (TopicPartition partition : partitions) {
                        Long endOffset = offset;
                        if (endOffset == null) {
                            Map<TopicPartition, Long> map = kafkaConsumer.endOffsets(Lists.newArrayList(partition), TIME_OUT_DURATION);
                            endOffset = map.get(partition);
                        }
                        recordsToDeleteMap.put(partition, RecordsToDelete.beforeOffset(endOffset));
                    }
                    DeleteRecordsOptions options = new DeleteRecordsOptions();
                    options.timeoutMs(TIME_OUT_MS);
                    DeleteRecordsResult deleteRecordsResult = kafkaAdminClient.deleteRecords(recordsToDeleteMap, options);
                    Map<TopicPartition, KafkaFuture<DeletedRecords>> map = deleteRecordsResult.lowWatermarks();
                    for (KafkaFuture<DeletedRecords> value : map.values()) {
                        try {
                            DeletedRecords deletedRecords = value.get();
                            deletedRecords.lowWatermark();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return "清除成功";
                }));
    }

    private boolean tryCreateTopicByKafkaAdminUrl(CreateTopicMsgReqVo reqVo) {
        String url = kafkaAdminUrl + "/clusters/kafka-test/topics/create";

        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        reqHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
        String reqBody = "topic=" + reqVo.getTopic() + "&partitions=" + reqVo.getNumPartitions() +
                "&replication=" + reqVo.getReplicationFactor();
        HttpEntity<String> reqEntity = new HttpEntity<>(reqBody, reqHeaders);

        ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.POST, reqEntity, String.class);
        String body = entity.getBody();
        return body != null && body.contains("Done!");
    }

    private boolean tryDelTopicByKafkaAdminUrl(String topic) {
        String url = kafkaAdminUrl + "/clusters/kafka-test/topics/delete?t=" + topic;

        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        reqHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
        String reqBody = "topic=" + topic;
        HttpEntity<String> reqEntity = new HttpEntity<>(reqBody, reqHeaders);

        ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.POST, reqEntity, String.class);
        String body = entity.getBody();
        if (body != null && body.contains("Done!")) {
            return true;
        }
        if (body != null && body.contains("does not exist")) {
            throw new BizException("Topic不存在:" + topic);
        }
        return false;
    }

    @Override
    public String sendTopicMsg(SendTopicMsgReqVo reqVo) {
        return execKafkaTemplate(reqVo.getConnectId(), kafkaTemplate -> {
            List<RecordHeader> headers = null;
            if (StringUtils.isNotBlank(reqVo.getHeaders())) {
                headers = Arrays.stream(reqVo.getHeaders().split(";"))
                        .map(split -> {
                            String[] strList = split.split("=");
                            return new RecordHeader(strList[0], strList[1].getBytes(StandardCharsets.UTF_8));
                        })
                        .collect(Collectors.toList());
            }
            @SuppressWarnings({"unchecked", "rawtypes"})
            ProducerRecord<Object, Object> producerRecord = new ProducerRecord(reqVo.getTopic(), reqVo.getPartition(),
                    reqVo.getKey(), reqVo.getValue(), headers);
            ListenableFuture<SendResult<Object, Object>> future = kafkaTemplate.send(producerRecord);
            try {
                SendResult<Object, Object> objectObjectSendResult = future.get();
                log.info("发送成功:{}", objectObjectSendResult);
            } catch (Exception e) {
                log.error("发送失败", e);
                throw new BizException("发送失败:" + e.getMessage());
            }
            return "发送成功";
        });
    }

    @Override
    public List<KafkaTree> getKafkaTrees(String connectId) {
        return execAdminClient(connectId, kafkaAdminClient -> {
            List<KafkaTree> kafkaTrees = Lists.newArrayList();
            kafkaTrees.add(getKafkaTreeBrokers(kafkaAdminClient));
            kafkaTrees.add(getKafkaTreeTopics(kafkaAdminClient));
            kafkaTrees.add(getKafkaTreeConsumers(kafkaAdminClient));
            return kafkaTrees;
        });
    }

    @Override
    public List<TopicInfo> getTopicInfo(String connectId, String topic) {
        return execKafkaConsumer(connectId, kafkaConsumer -> {
            List<PartitionInfo> partitionInfoList = kafkaConsumer.partitionsFor(topic, TIME_OUT_DURATION);
            return partitionInfoList.stream()
                    .map(partition -> TopicInfo.builder()
                            .partition(partition.partition())
                            .leader(partition.leader().idString())
                            .replicas(Arrays.stream(partition.replicas())
                                    .map(Node::idString)
                                    .collect(Collectors.toList()))
                            .isr(Arrays.stream(partition.inSyncReplicas())
                                    .map(Node::idString)
                                    .collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toList());
        });
    }

    private List<TopicPartition> getTopicPartition(String topic, Integer partition,
                                                   KafkaConsumer<Object, Object> kafkaConsumer) {
        List<PartitionInfo> partitionInfoList = kafkaConsumer.partitionsFor(topic, TIME_OUT_DURATION);
        Collection<TopicPartition> partitions = partitionInfoList.stream()
                .map(it -> new TopicPartition(topic, it.partition()))
                .collect(Collectors.toList());

        List<TopicPartition> topicPartitions;
        if (partition != null) {
            TopicPartition topicPartition = new TopicPartition(topic, partition);
            topicPartitions = Lists.newArrayList(topicPartition);
        } else {
            Map<TopicPartition, Long> map = kafkaConsumer.endOffsets(partitions, TIME_OUT_DURATION);
            topicPartitions = map.entrySet().stream()
                    .filter(it -> it.getValue() > 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
        return topicPartitions;
    }

    @Override
    public List<TopicMsg> getTopicMsgList(String connectId, String topic, GetTopicReqVo reqVo) {
        return execKafkaConsumer(connectId, kafkaConsumer -> {
            List<TopicPartition> topicPartitions = getTopicPartition(topic, reqVo.getPartition(), kafkaConsumer);
            if (topicPartitions.isEmpty()) {
                return Collections.emptyList();
            }

            kafkaConsumer.assign(topicPartitions);

            if (reqVo.getOffset() != null) {
                for (TopicPartition topicPartition : topicPartitions) {
                    kafkaConsumer.seek(topicPartition, reqVo.getOffset());
                }
            } else {
                kafkaConsumer.seekToBeginning(topicPartitions);
            }

            int i = 0;
            List<TopicMsg> topicMsgList = Lists.newArrayList();
            outer:
            while (true) {
                ConsumerRecords<Object, Object> records = kafkaConsumer.poll(Duration.ofMillis(3000));
                if (records.isEmpty()) {
                    break;
                }
                for (ConsumerRecord<Object, Object> record : records) {
                    i++;
                    String headers = Arrays.stream(record.headers().toArray())
                            .map(header -> header.key() + "=" + Arrays.toString(header.value()))
                            .collect(Collectors.joining(";"));
                    String value = record.value().toString();
                    if (reqVo.getValue() != null) {
                        if (!value.contains(reqVo.getValue())) {
                            continue;
                        }
                    }
                    long timestamp = record.timestamp();
                    if (reqVo.getTimestamp() != null) {
                        if (timestamp < reqVo.getTimestamp()) {
                            continue;
                        }
                    }
                    TopicMsg topicMsg = TopicMsg.builder()
                            .key(record.key())
                            .value(value)
                            .partition(record.partition())
                            .offset(record.offset())
                            .timestamp(new Date(timestamp))
                            .headers(headers)
                            .build();
                    topicMsgList.add(topicMsg);
                    if (topicMsgList.size() >= 300) {
                        break outer;
                    }
                    if (i > 3000) {
                        throw new BizException("检索记录数过多(已超过3000),因此中断查询,请修改查询条件!");
                    }
                }
            }
            return topicMsgList;
        });
    }

    @SneakyThrows
    private KafkaTree getKafkaTreeBrokers(KafkaAdminClient kafkaAdminClient) {
        DescribeClusterOptions options = new DescribeClusterOptions();
        options.timeoutMs(TIME_OUT_MS);
        Collection<Node> nodes = kafkaAdminClient.describeCluster(options).nodes().get();
        List<KafkaTree> brokers = nodes.stream()
                .map(node -> KafkaTree.builder()
                        .label(node.host() + ":" + node.port() + "(" + node.idString() + ")")
                        .isLeaf(true)
                        .kafkaNodeType("Broker")
                        .build())
                .sorted(Comparator.comparing(KafkaTree::getLabel))
                .collect(Collectors.toList());

        return KafkaTree.builder()
                .label("Brokers(" + brokers.size() + ")")
                .isLeaf(false)
                .kafkaNodeType("Brokers")
                .children(brokers)
                .build();
    }

    @SneakyThrows
    private KafkaTree getKafkaTreeTopics(KafkaAdminClient kafkaAdminClient) {
        ListTopicsOptions options = new ListTopicsOptions();
        options.timeoutMs(TIME_OUT_MS);
        ListTopicsResult listTopicsResult = kafkaAdminClient.listTopics(options);
        Set<String> topics = listTopicsResult.names().get();
        List<KafkaTree> topicList = topics.stream()
                .map(topic -> KafkaTree.builder()
                        .label(topic)
                        .isLeaf(true)
                        .kafkaNodeType("Topic")
                        .build())
                .sorted(Comparator.comparing(KafkaTree::getLabel))
                .collect(Collectors.toList());

        return KafkaTree.builder()
                .label("Topics(" + topicList.size() + ")")
                .isLeaf(false)
                .kafkaNodeType("Topics")
                .children(topicList)
                .build();
    }

    @SneakyThrows
    private KafkaTree getKafkaTreeConsumers(KafkaAdminClient kafkaAdminClient) {
        ListConsumerGroupsOptions options = new ListConsumerGroupsOptions();
        options.timeoutMs(TIME_OUT_MS);
        ListConsumerGroupsResult listConsumerGroupsResult = kafkaAdminClient.listConsumerGroups(options);
        Collection<ConsumerGroupListing> consumerGroupListings = listConsumerGroupsResult.valid().get();
        List<KafkaTree> consumers = consumerGroupListings.stream()
                .map(groupListing -> KafkaTree.builder()
                        .label(groupListing.groupId())
                        .isLeaf(true)
                        .kafkaNodeType("Consumer")
                        .build())
                .sorted(Comparator.comparing(KafkaTree::getLabel))
                .collect(Collectors.toList());

        return KafkaTree.builder()
                .label("Consumers(" + consumers.size() + ")")
                .isLeaf(false)
                .kafkaNodeType("Consumers")
                .children(consumers)
                .build();
    }
}
