package com.neighborhelp.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.config.OssProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OssService {

    private final OssProperties ossProperties;

    public String uploadFile(MultipartFile file) {
        String endpoint = ossProperties.getEndpoint();
        String accessKeyId = ossProperties.getAccessKeyId();
        String accessKeySecret = ossProperties.getAccessKeySecret();
        String bucketName = ossProperties.getBucketName();

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        
        try {
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            // 获取文件后缀名
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            // 生成唯一文件名，按文件夹分类
            String fileName = "avatars/" + UUID.randomUUID().toString().replace("-", "") + extension;

            // 上传文件到 OSS
            ossClient.putObject(bucketName, fileName, inputStream);

            // 构建并返回访问文件的 URL (针对公共读的 bucket)
            return "https://" + bucketName + "." + endpoint + "/" + fileName;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "文件上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}