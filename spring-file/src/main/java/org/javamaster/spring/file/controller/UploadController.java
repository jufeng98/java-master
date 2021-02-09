package org.javamaster.spring.file.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.javamaster.spring.file.model.Result;
import org.javamaster.spring.file.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author yudong
 * @date 2021/2/8
 */
@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @SneakyThrows
    @PostMapping("/uploadFile")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile multipartFile, UriComponentsBuilder builder) {
        log.info("{},{},{},{}", multipartFile.getName(), multipartFile.getContentType(),
                multipartFile.getSize(), multipartFile.getResource().getFilename());
        return new Result<>(uploadService.uploadFile(multipartFile.getOriginalFilename(), multipartFile.getBytes(), builder));
    }

    @SneakyThrows
    @PostMapping("/checkBigFile")
    public Result<Object> checkBigFile(String fileName, String fileMd5, Integer fileSize, Integer chunkSize,
                                       UriComponentsBuilder builder) {
        return new Result<>(uploadService.checkBigFile(fileName, fileMd5, fileSize, chunkSize, builder));
    }

    @SneakyThrows
    @PostMapping("/uploadBigFile")
    public Result<String> uploadBigFile(Integer chunk, String fileMd5, String chunkMd5, MultipartFile file) {
        return new Result<>(uploadService.uploadBigFile(chunk, fileMd5, chunkMd5, file.getBytes()));

    }

    @SneakyThrows
    @PostMapping("/mergeBigFile")
    public Result<String> mergeBigFile(String fileMd5, String fileName, UriComponentsBuilder builder) {
        return new Result<>(uploadService.mergeBigFile(fileMd5, fileName, builder));
    }

}
