package com.baidu.shop.upload.controller;

import com.baidu.shop.base.Result;
import com.baidu.shop.status.HTTPStatus;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @ClassName FastDFSUploadController
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/4
 * @Version V1.0
 **/
@RestController
@RequestMapping(value = "upload")
@Slf4j
public class FastDFSUploadController {
    //图片服务器的地址
    @Value(value = "${mingrui.upload.img.host}")
    private String imgHost;

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;


    @PostMapping
    public Result<String> uploadImg(@RequestParam MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();//获取文件输入流

        String filename = file.getOriginalFilename();//文件名

        String ex = filename.substring(filename.lastIndexOf(".") + 1);//文件后缀名
        // 上传并且生成缩略图
        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(
                inputStream, file.getSize(), ex, null);//上传
        // 带分组的路径
        log.info("上传图片全路径:{}", storePath.getFullPath());
        // 不带分组的路径
        log.info("上传图片路径:{}", storePath.getPath());
        // 获取缩略图路径
        String path = thumbImageConfig.getThumbImagePath(storePath.getFullPath());
        log.info("缩略图路径:{}", path);

        return new Result<String>(HTTPStatus.OK,"上传成功",imgHost + path);
    }
}
