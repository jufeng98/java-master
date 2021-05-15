package org.javamaster.spring.test.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;

import java.util.HashMap;
import java.util.Map;

import static org.javamaster.spring.test.GeneralTestCode.PROFILE_UNIT_TEST;

/**
 * @author yudong
 * @date 2021/5/15
 */
@Slf4j
@SuppressWarnings("all")
@TestConfiguration
@Profile(PROFILE_UNIT_TEST)
public class KafkaTestConfig {
    @Value("${spring.test.kafka.bootstrap.servers}")
    private String servers;

    @Bean
    public ProducerFactory producerFactory() {
        Map<String, Object> configs = new HashMap<>(20, 1);
        configs.put("bootstrap.servers", servers);
        configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configs.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configs.put("group.id", "clean_service");
        configs.put("enable.auto.commit", "true");
        configs.put("auto.commit.interval", "100");
        configs.put("auto.offset.reset", "earliest");
        configs.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        configs.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        configs.put("buffer.memory", "33554432");
        configs.put("batch.size", "16384");
        configs.put("retries", "3");
        configs.put("acks", "1");
        return new DefaultKafkaProducerFactory(configs);
    }

    @Bean
    public KafkaTemplate kafkaTemplateCluster(ProducerFactory producerFactory) {
        KafkaTemplate kafkaTemplate = new KafkaTemplate(producerFactory);
        kafkaTemplate.setProducerListener(new ProducerListener<Object, Object>() {
            @Override
            public void onSuccess(String topic, Integer partition, Object key, Object value, RecordMetadata recordMetadata) {
                log.info("{}", "success:" + value);
            }

            @Override
            public void onError(String topic, Integer partition, Object key, Object value, Exception exception) {
                log.error("{}", "error:" + value);
            }
        });
        return kafkaTemplate;
    }

}
