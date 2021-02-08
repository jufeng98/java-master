package org.javamaster.spring.file.service;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author yudong
 * @date 2021/2/8
 */
public interface UploadService {
    /**
     * 文件上传
     *
     * @param fileName
     * @param fileBytes
     * @param builder
     * @return 文件访问路径
     */
    String uploadFile(String fileName, byte[] fileBytes, UriComponentsBuilder builder);

    /**
     * 大文件上传,检查上传情况
     *
     * @param fileName
     * @param fileMd5
     * @param fileSize
     * @param chunkSize
     * @param builder
     * @return 若已上传过, 则返回文件访问路径, 否则返回缺失的分片数组
     */
    Object checkBigFile(String fileName, String fileMd5, Integer fileSize, Integer chunkSize,
                        UriComponentsBuilder builder);

    /**
     * 大文件上传,上传分片
     *
     * @param chunk
     * @param fileMd5
     * @param chunkMd5
     * @param chunkBytes
     * @return
     */
    String uploadBigFile(Integer chunk, String fileMd5, String chunkMd5, byte[] chunkBytes);

    /**
     * 大文件上传,合并分片
     *
     * @param fileMd5
     * @param fileName
     * @param builder
     * @return 文件访问路径
     */
    String mergeBigFile(String fileMd5, String fileName, UriComponentsBuilder builder);
}
