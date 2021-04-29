package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.upload.controller.UploadController;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    //定义文件的类型的字符串集合
    private static final List<String> CONTEXT_TYPE = Arrays.asList("image/png", "image/jpeg", "image/gif");

    @Autowired
    private FastFileStorageClient storageClient;

    public String uploadImage(MultipartFile file) {
        try {
            //获取文件的类型
            String contentType = file.getContentType();
            //判断文件类型中是否有次文件类型
            if (!CONTEXT_TYPE.contains(contentType)) {
                LOGGER.warn("{}上传失败，文件类型不正确", contentType);
                return null;
            }

            //判断内容是否为图片
            BufferedImage context = ImageIO.read(file.getInputStream());
            if (context == null) {
                LOGGER.warn("上传文件失败，文件内容不符合要求");
                return null;
            }

            //保存图片
            //       file.transferTo(new File("/Users/wentimei/Desktop/picture/" + file.getOriginalFilename()));
            //获取文件后缀名
            String extension=StringUtils.substringAfterLast(file.getOriginalFilename(),".");

            //上传
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);


            //拼接地址
            String url = "http://image.leyou.com/" + storePath.getFullPath();
            return url;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
