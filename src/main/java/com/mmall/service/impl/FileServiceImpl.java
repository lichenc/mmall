package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    public String upload(MultipartFile file ,String path)
    {
        String fileName = file.getOriginalFilename();
        //扩展名,123.jpg  获取 . 的位置并加一
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        //为了上传的文件名有重复情况，生成唯一的文件名
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        //
        logger.info("开始上传文件，上传文件的文件名：{}，上传的路径：{}，新文件名：{}",fileName,path,uploadFileName);
        //新建文件目录对象
        File fileDir = new File(path);
        //判断目录是否存在，不存在则创建
        if(!fileDir.exists())
        {
            //设置可写权限
            fileDir.setWritable(true);
            //创建目录
            fileDir.mkdirs();
        }
        //创建目标目录
        File targetFile = new File(path,uploadFileName);
        try {
            file.transferTo(targetFile);
            //文件已经上传

            //将targetFile上传到我们的FTP服务器
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //上传完成
            //上传完后，将本地upload的文件删除
            targetFile.delete();
        } catch (IOException e) {
            logger.error("文件上传失败",e);
        }

        return targetFile.getName();
    }
}
