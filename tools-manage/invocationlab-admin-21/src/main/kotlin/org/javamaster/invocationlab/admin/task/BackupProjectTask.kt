package org.javamaster.invocationlab.admin.task

import org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX
import org.javamaster.invocationlab.admin.util.SpringUtils
import com.google.common.collect.Lists
import org.apache.commons.lang3.tuple.Pair
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.util.FileSystemUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream


object BackupProjectTask {
    private val log: Logger = LoggerFactory.getLogger(BackupProjectTask::class.java)

    private const val THRESHOLD = (86400 * 10).toLong()

    fun startTask() {
        if (SpringUtils.proEnv || SpringUtils.devEnv) {
            return
        }
        val scheduledExecutorService = Executors.newScheduledThreadPool(1)
        scheduledExecutorService.scheduleWithFixedDelay({
            try {
                delExpireDir()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            val simpleDateFormat = SimpleDateFormat("yyyyMMdd")
            val path = File("backup" + File.separator + simpleDateFormat.format(Date()))
            if (!path.exists()) {
                try {
                    Files.createDirectories(path.toPath())
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            log.info("备份路径:{}", path.absolutePath)

            val stringRedisTemplate = SpringUtils.context.getBean(
                StringRedisTemplate::class.java
            )
            val list = getProjects(stringRedisTemplate)
            for (pair in list) {
                try {
                    saveProject(pair.left, pair.right, path)
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
            log.info("完成 {} 个项目备份", list.size)
        }, 7200, 86400, TimeUnit.SECONDS)
    }


    private fun saveProject(name: String, bytes: ByteArray, path: File) {
        var tmpName = name
        tmpName = tmpName.replace(":", "-")
        val destDir = File(path, "$tmpName.gzip")
        FileOutputStream(destDir).use { outputStream ->
            GZIPOutputStream(outputStream).use { gzipOutputStream ->
                gzipOutputStream.write(bytes)
                gzipOutputStream.finish()
            }
        }
        TimeUnit.SECONDS.sleep(2)
    }

    private fun getProjects(stringRedisTemplate: StringRedisTemplate): List<Pair<String, ByteArray>> {
        return stringRedisTemplate.execute<List<Pair<String, ByteArray>>>(
            RedisCallback { connection: RedisConnection ->
                val scanOptions: ScanOptions = ScanOptions.scanOptions()
                    .match(ERD_PREFIX + "20*").count(10000).build()
                try {
                    connection.keyCommands().scan(scanOptions).use { cursor ->
                        val list: MutableList<Pair<String, ByteArray>> = Lists.newArrayList()
                        while (cursor.hasNext()) {
                            val keyBytes = cursor.next()
                            val key = String(keyBytes, StandardCharsets.UTF_8)
                            val bytes = connection.stringCommands()[keyBytes]
                            list.add(Pair.of(key, bytes))
                        }
                        return@RedisCallback list
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            })!!
    }


    private fun delExpireDir() {
        val path = File("backup")
        val now = Instant.now()
        Files.walkFileTree(path.toPath(), setOf(FileVisitOption.FOLLOW_LINKS), 1,
            object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val instant = attrs.creationTime().toInstant()
                    val second = now.minusSeconds(instant.epochSecond).epochSecond
                    if (second > THRESHOLD) {
                        FileSystemUtils.deleteRecursively(path)
                        log.info("删掉过期文件夹:{}", path.toAbsolutePath())
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                    return FileVisitResult.CONTINUE
                }
            })
    }
}
