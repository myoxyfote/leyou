package com.leyou.upload.controller;

import com.leyou.upload.service.UploadService;
import com.netflix.ribbon.proxy.annotation.Http;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HttpsURLConnection;

@Controller
@RequestMapping("upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping("image")
    public ResponseEntity<String> uploadImage(@RequestParam("file")MultipartFile file){
        //获取图片的地址
        String url = uploadService.uploadImage(file);
        //如果url为空 400
        if(StringUtils.isBlank(url)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

         return ResponseEntity.ok(url);

    }
}
