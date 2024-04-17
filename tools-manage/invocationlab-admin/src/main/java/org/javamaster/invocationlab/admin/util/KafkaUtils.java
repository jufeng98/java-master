package org.javamaster.invocationlab.admin.util;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;

import java.util.Map;

@Slf4j
public class KafkaUtils {

    public static KafkaConsumer<Object, Object> kafkaConsumerSingleton(String connectId, String nodes) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext()
                .getAutowireCapableBeanFactory();
        String id = connectId + ":kafkaConsumer";
        if (beanFactory.containsBean(id)) {
            //noinspection unchecked
            return (KafkaConsumer<Object, Object>) beanFactory.getBean(id);
        }

        KafkaConsumer<Object, Object> kafkaConsumer = newKafkaConsumer(nodes);
        beanFactory.registerSingleton(id, kafkaConsumer);
        return kafkaConsumer;
    }

    @NotNull
    private static KafkaConsumer<Object, Object> newKafkaConsumer(String nodes) {
        Map<String, Object> configs = Maps.newHashMap();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, nodes);

        String name = StringDeserializer.class.getName();
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, name);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, name);

        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "invocationlab_admin_consumer");
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return new KafkaConsumer<>(configs);
    }

    public static KafkaAdminClient kafkaAdminClientSingleton(String connectId, String nodes) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext()
                .getAutowireCapableBeanFactory();
        String id = connectId + ":kafkaAdminClient";
        if (beanFactory.containsBean(id)) {
            return (KafkaAdminClient) beanFactory.getBean(id);
        }

        Map<String, Object> prop = Maps.newHashMap();
        prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, nodes);
        KafkaAdminClient kafkaAdminClient = (KafkaAdminClient) KafkaAdminClient.create(prop);
        beanFactory.registerSingleton(id, kafkaAdminClient);
        return kafkaAdminClient;
    }

    public static DefaultKafkaProducerFactory<Object, Object> newKafkaProducerFactory(String nodes) {
        Map<String, Object> configs = Maps.newHashMap();
        String name = StringSerializer.class.getName();
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, name);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, name);

        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, nodes);
        configs.put(ProducerConfig.CLIENT_ID_CONFIG, "invocationlab_admin_producer");
        configs.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 6000);
        configs.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configs.put(ProducerConfig.RETRIES_CONFIG, 0);
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(configs);
    }

    public static DefaultKafkaProducerFactory<Object, Object> kafkaProducerFactorySingleton(String connectId, String nodes) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext()
                .getAutowireCapableBeanFactory();
        String id = connectId + ":ProducerFactory";
        if (beanFactory.containsBean(id)) {
            //noinspection unchecked
            return (DefaultKafkaProducerFactory<Object, Object>) beanFactory.getBean(id);
        }

        DefaultKafkaProducerFactory<Object, Object> producerFactory = newKafkaProducerFactory(nodes);
        beanFactory.registerSingleton(id, producerFactory);
        return producerFactory;
    }

    public static KafkaTemplate<Object, Object> kafkaTemplateSingleton(String connectId, String nodes) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext()
                .getAutowireCapableBeanFactory();
        String id = connectId + ":kafkaTemplate";
        if (beanFactory.containsBean(id)) {
            //noinspection unchecked
            return (KafkaTemplate<Object, Object>) beanFactory.getBean(id);
        }

        ProducerFactory<Object, Object> producerFactory = kafkaProducerFactorySingleton(connectId, nodes);
        KafkaTemplate<Object, Object> kafkaTemplate = newKafkaTemplate(producerFactory);

        beanFactory.registerSingleton(id, kafkaTemplate);
        return kafkaTemplate;
    }

    @NotNull
    private static KafkaTemplate<Object, Object> newKafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setProducerListener(new ProducerListener<Object, Object>() {
            public void onSuccess(String topic, Integer partition, Object key, Object value, RecordMetadata recordMetadata) {
                log.info("success:{},{},{}", topic, partition, value);
            }

            public void onError(String topic, Integer partition, Object key, Object value, Exception exception) {
                log.info("error:{},{},{}", topic, partition, value, exception);
            }
        });
        return kafkaTemplate;
    }
}
