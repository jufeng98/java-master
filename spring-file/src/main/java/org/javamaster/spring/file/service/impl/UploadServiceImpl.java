package org.javamaster.spring.file.service.impl;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.javamaster.spring.file.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletContext;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author yudong
 * @date 2021/2/8
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {
    @Autowired
    private ServletContext servletContext;

    @Override
    @SneakyThrows
    public String uploadFile(String fileName, byte[] fileBytes, UriComponentsBuilder builder) {
        // 简单起见,直接保存到webapp的file目录下以便能访问
        File file = new File(servletContext.getRealPath("/") + "/file", fileName);
        @Cleanup
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        StreamUtils.copy(fileBytes, fileOutputStream);
        return builder.path("file").path("/").path(fileName).build().toUriString();
    }

    @Override
    @SneakyThrows
    @PostMapping("/checkBigFile")
    public Object checkBigFile(String fileName, String fileMd5, Integer fileSize, Integer chunkSize,
                               UriComponentsBuilder builder) {
        File filePath = new File(servletContext.getRealPath("/") + "/file", fileMd5);
        if (!filePath.exists()) {
            Files.createDirectories(filePath.toPath());
        }
        File currentFile = new File(filePath, fileName);
        if (currentFile.exists()) {
            // 文件已经被成功上传过,直接返回url
            return builder.path("file").path("/").path(fileMd5).path("/").path(fileName).build().toUriString();
        }
        // 计算文件总分片数
        int chunkNum = (int) Math.ceil(1.0 * fileSize / chunkSize);
        List<Integer> chunks = IntStream.range(0, chunkNum).boxed().collect(Collectors.toList());
        // 得到已上传的文件片号
        List<Integer> chunkNames = Files.walk(filePath.toPath())
                .filter(Files::isRegularFile)
                .map(path -> Integer.parseInt(path.getFileName().toString()))
                .collect(Collectors.toList());
        // 得到缺失的文件片号
        chunks.removeAll(chunkNames);
        return chunks;
    }

    @Override
    @SneakyThrows
    public String uploadBigFile(Integer chunk, String fileMd5, String chunkMd5, byte[] chunkBytes) {
        String md5 = DigestUtils.md5DigestAsHex(chunkBytes);
        Assert.isTrue(chunkMd5.equals(md5), "分片已损坏,请重新上传");
        File filePath = new File(servletContext.getRealPath("/") + "/file/" + fileMd5, String.valueOf(chunk));
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            StreamUtils.copy(chunkBytes, fileOutputStream);
        } catch (Exception e) {
            Files.delete(filePath.toPath());
            throw e;
        }
        return "分片上传成功";
    }

    @Override
    @SneakyThrows
    public String mergeBigFile(String fileMd5, String fileName, UriComponentsBuilder builder) {
        File filePath = new File(servletContext.getRealPath("/") + "/file", fileMd5);
        File currentFile = new File(filePath, fileName);
        if (currentFile.exists()) {
            // 文件已经被成功合并过,直接返回url
            return builder.path("file").path("/").path(fileMd5).path("/").path(fileName).build().toUriString();
        }
        List<Path> chunkFiles = Files.walk(filePath.toPath())
                .filter(Files::isRegularFile)
                .sorted()
                .collect(Collectors.toList());
        // 分片合并
        @Cleanup
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (Path chunkFile : chunkFiles) {
            byte[] chunkFileBytes = Files.readAllBytes(chunkFile);
            byteArrayOutputStream.write(chunkFileBytes);
            Files.delete(chunkFile);
        }
        byte[] fileBytes = byteArrayOutputStream.toByteArray();
        @Cleanup
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(currentFile));
        StreamUtils.copy(fileBytes, bufferedOutputStream);
        return builder.path("file").path("/").path(fileMd5).path("/").path(fileName).build().toUriString();
    }
}
