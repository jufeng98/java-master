package org.javamaster.invocationlab.admin.util;

import com.dbschema.MongoJdbcDriver;
import com.dbschema.mongo.MongoConnection;
import com.dbschema.mongo.MongoService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.common.collect.Lists;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.SneakyThrows;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.IndexsBean;
import org.javamaster.invocationlab.admin.model.erd.PropertiesBean;
import org.javamaster.invocationlab.admin.redis.Function;
import org.javamaster.invocationlab.admin.serializer.BigDecimalToJsonSerializer;
import org.javamaster.invocationlab.admin.serializer.BigIntegerToJsonSerializer;
import org.javamaster.invocationlab.admin.serializer.ByteArrayToJsonSerializer;
import org.javamaster.invocationlab.admin.serializer.LongToJsonSerializer;
import org.javamaster.invocationlab.admin.serializer.ObjectIdSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MongoUtils {
    public static ObjectMapper mongoObjectMapper;
    public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    static {
        mongoObjectMapper = Jackson2ObjectMapperBuilder.json().build();
        mongoObjectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mongoObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mongoObjectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        mongoObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(BigDecimal.class, BigDecimalToJsonSerializer.INSTANCE);
        simpleModule.addSerializer(BigInteger.class, BigIntegerToJsonSerializer.INSTANCE);
        simpleModule.addSerializer(Long.class, new LongToJsonSerializer());
        simpleModule.addSerializer(byte[].class, new ByteArrayToJsonSerializer());
        simpleModule.addSerializer(ObjectId.class, ObjectIdSerializer.INSTANCE);
        mongoObjectMapper.registerModule(simpleModule);

        mongoObjectMapper.setTimeZone(UTC_TIME_ZONE);

        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN);
        dateFormat.setTimeZone(UTC_TIME_ZONE);

        mongoObjectMapper.setDateFormat(dateFormat);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(PATTERN)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        mongoObjectMapper.registerModule(javaTimeModule);
    }

    public static ConversionService getConversionServiceSingle() {
        String name = "mongoConversionService";
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getContext().getAutowireCapableBeanFactory();

        if (!beanFactory.containsBean(name)) {
            WebConversionService conversionService = new WebConversionService(null);
            //noinspection Convert2Lambda
            conversionService.addConverter(new Converter<String, Date>() {
                @Override
                public Date convert(@NotNull String source) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
                        sdf.setTimeZone(UTC_TIME_ZONE);
                        return sdf.parse(source);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            beanFactory.registerSingleton(name, conversionService);
        }

        return (ConversionService) SpringUtils.getContext().getBean(name);
    }

    public static String resolveUrlDbName(String url) {
        URI uri = URI.create(url.substring(5));
        return uri.getPath().substring(1);
    }

    private static final ConcurrentMap<String, BlockingQueue<MongoConnection>> CONNECTION_MAP = new ConcurrentHashMap<>();

    @SneakyThrows
    public static void close() {
        for (BlockingQueue<MongoConnection> connectionPool : CONNECTION_MAP.values()) {
            MongoConnection connection = connectionPool.poll();
            while (connection != null) {
                connection.close();
                connection = connectionPool.poll();
            }
        }
    }

    @SneakyThrows
    public static <T> T executeMongo(DbsBean dbsBean, Function<MongoConnection, T> callback) {
        PropertiesBean properties = dbsBean.getProperties();

        Properties info = new Properties();
        info.setProperty("user", properties.getUsername());
        info.setProperty("password", properties.getPassword());
        info.setProperty("connectTimeoutMS", "6000");

        BlockingQueue<MongoConnection> connectionPool = CONNECTION_MAP.get(properties.unique());
        if (connectionPool == null) {
            connectionPool = new LinkedBlockingDeque<>();
            CONNECTION_MAP.put(properties.unique(), connectionPool);
        }

        if (connectionPool.size() < 3) {
            MongoJdbcDriver mongoJdbcDriver = new MongoJdbcDriver();
            MongoConnection connection = (MongoConnection) mongoJdbcDriver.connect(properties.getUrl(), info);
            connectionPool.put(connection);
        }

        MongoConnection connection = connectionPool.poll(3, TimeUnit.SECONDS);
        if (connection == null) {
            throw new RuntimeException("获取连接超时");
        }

        try {
            T result = callback.apply(connection);
            connectionPool.put(connection);
            return result;
        } catch (ErdException | BizException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T executeMongo(DbsBean dbsBean, String collectionName, Function<MongoCollection<Document>, T> callback) {
        return executeMongo(dbsBean, connection -> executeMongo(collectionName, connection, callback));
    }

    @SneakyThrows
    public static <T> T executeMongo(String collectionName, MongoConnection connection, Function<MongoCollection<Document>, T> callback) {
        String dbName = resolveUrlDbName(connection.getUrl());
        MongoService service = connection.getService();
        MongoCollection<Document> collection = service.getDatabase(dbName).getCollection(collectionName);
        return callback.apply(collection);
    }

    public static List<IndexsBean> listIndexes(DbsBean dbsBean, String collectionName) {
        return MongoUtils.executeMongo(dbsBean, collectionName, documentMongoCollection -> {
            List<IndexsBean> indexesBeans = Lists.newArrayList();

            ListIndexesIterable<Document> indexesIterable = documentMongoCollection.listIndexes();
            try (MongoCursor<Document> iterator = indexesIterable.iterator()) {
                while (iterator.hasNext()) {
                    Document document = iterator.next();
                    IndexsBean indexsBean = new IndexsBean();
                    indexsBean.setName(document.getString("name"));
                    Document doc = (Document) document.get("key");
                    List<String> list = doc.entrySet().stream()
                            .map(it -> it.getKey() + "(" + it.getValue() + ")")
                            .collect(Collectors.toList());
                    indexsBean.setFields(list);
                    indexsBean.setIsUnique(false);
                    indexesBeans.add(indexsBean);
                }
                iterator.close();
                return indexesBeans;
            }
        });
    }
}
