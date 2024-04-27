package org.javamaster.invocationlab.admin.util;

import com.dbschema.MongoJdbcDriver;
import com.dbschema.mongo.MongoConnection;
import com.dbschema.mongo.MongoService;
import com.google.common.collect.Lists;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.SneakyThrows;
import org.bson.Document;
import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.IndexsBean;
import org.javamaster.invocationlab.admin.model.erd.PropertiesBean;
import org.javamaster.invocationlab.admin.redis.Function;

import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MongoUtils {
    public static String resolveUrlDbName(String url) {
        URI uri = URI.create(url.substring(5));
        return uri.getPath().substring(1);
    }

    private static final BlockingQueue<MongoConnection> CONNECTION_POOL = new LinkedBlockingDeque<>();

    @SneakyThrows
    public static void close() {
        MongoConnection connection = CONNECTION_POOL.poll();
        while (connection != null) {
            connection.close();
            connection = CONNECTION_POOL.poll();
        }
    }

    @SneakyThrows
    public static <T> T executeMongo(DbsBean dbsBean, Function<MongoConnection, T> callback) {
        PropertiesBean properties = dbsBean.getProperties();

        Properties info = new Properties();
        info.setProperty("user", properties.getUsername());
        info.setProperty("password", properties.getPassword());
        info.setProperty("connectTimeoutMS", "6000");

        if (CONNECTION_POOL.size() < 3) {
            MongoJdbcDriver mongoJdbcDriver = new MongoJdbcDriver();
            MongoConnection connection = (MongoConnection) mongoJdbcDriver.connect(properties.getUrl(), info);
            CONNECTION_POOL.put(connection);
        }

        MongoConnection connection = CONNECTION_POOL.poll(3, TimeUnit.SECONDS);
        if (connection == null) {
            throw new RuntimeException("获取连接超时");
        }

        try {
            T result = callback.apply(connection);
            CONNECTION_POOL.put(connection);
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
