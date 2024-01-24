package org.javamaster.invocationlab.admin.task;

import org.javamaster.invocationlab.admin.util.SpringUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX;

@Slf4j
public class BackupProjectTask {
    private static final long THRESHOLD = 86400 * 10;

    public static void startTask() {
        if (SpringUtils.isProEnv() || SpringUtils.isDevEnv()) {
            return;
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                delExpireDir();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            File path = new File("backup" + File.separator + simpleDateFormat.format(new Date()));
            if (!path.exists()) {
                try {
                    Files.createDirectories(path.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            log.info("备份路径:{}", path.getAbsolutePath());

            StringRedisTemplate stringRedisTemplate = SpringUtils.getContext().getBean(StringRedisTemplate.class);
            List<Pair<String, byte[]>> list = getProjects(stringRedisTemplate);
            for (Pair<String, byte[]> pair : list) {
                try {
                    saveProject(pair.getLeft(), pair.getRight(), path);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            log.info("完成 {} 个项目备份", list.size());
        }, 7200, 86400, TimeUnit.SECONDS);
    }

    private static void saveProject(String name, byte[] bytes, File path) throws Exception {
        name = name.replace(":", "-");
        File destDir = new File(path, name + ".gzip");
        try (
                FileOutputStream outputStream = new FileOutputStream(destDir);
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)
        ) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
        }
        TimeUnit.SECONDS.sleep(2);
    }

    private static List<Pair<String, byte[]>> getProjects(StringRedisTemplate stringRedisTemplate) {
        return stringRedisTemplate.execute((RedisCallback<List<Pair<String, byte[]>>>) connection -> {
            ScanOptions scanOptions = new ScanOptions.ScanOptionsBuilder()
                    .match(ERD_PREFIX + "20*").count(10000).build();
            try (Cursor<byte[]> cursor = connection.scan(scanOptions)) {
                List<Pair<String, byte[]>> list = Lists.newArrayList();
                while (cursor.hasNext()) {
                    byte[] keyBytes = cursor.next();
                    String key = new String(keyBytes, StandardCharsets.UTF_8);
                    byte[] bytes = connection.get(keyBytes);
                    list.add(Pair.of(key, bytes));
                }
                return list;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void delExpireDir() throws Exception {
        File path = new File("backup");
        Instant now = Instant.now();
        Files.walkFileTree(path.toPath(), Collections.singleton(FileVisitOption.FOLLOW_LINKS), 1,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                        Instant instant = attrs.creationTime().toInstant();
                        long second = now.minusSeconds(instant.getEpochSecond()).getEpochSecond();
                        if (second > THRESHOLD) {
                            FileSystemUtils.deleteRecursively(path);
                            log.info("删掉过期文件夹:{}", path.toAbsolutePath());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }
                });
    }
}
