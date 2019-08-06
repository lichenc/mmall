package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FTPUtil {
    private static Logger logger = LoggerFactory.getLogger(FTPUtil.class);
    //静态变量
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUserName = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPassword = PropertiesUtil.getProperty("ftp.pass");
    //变量
    private String ip;
    private Integer port;
    private String user;
    private String pass;
    private FTPClient ftpClient;
    //构造方法
    public FTPUtil(String ip,int port,String user,String pass)
    {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pass = pass;
    }
    //定义方法
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUserName,ftpPassword);
        logger.info("开始连接ftp服务器");
        boolean result = ftpUtil.uploadFile("img",fileList);
        logger.info("结束上传，上传结果是：{}",result);
        return result;
    }
    //remoteFile 是将文件上传到ftp服务器下的文件夹
    private boolean uploadFile(String remoteFile,List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        if(connectServer(this.getIp(),this.port,this.user,this.pass))
        {
            try {
                //改变工作目录
                ftpClient.changeWorkingDirectory(remoteFile);
                //定义缓存大小
                ftpClient.setBufferSize(1024);
                //定义编码
                ftpClient.setControlEncoding("UTF-8");
                //定义允许本地模式
                ftpClient.enterLocalPassiveMode();
                //定义文件类型为二进制
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                //循环上传文件
                for (File fileItem : fileList){
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(),fis);
                }
            } catch (IOException e) {
                uploaded =false;
                e.printStackTrace();
            }finally {
                fis.close();
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }
    private boolean connectServer(String ip,Integer port,String user,String pass)
    {
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user,pass);
        } catch (IOException e) {
            logger.error("连接ftp服务器异常",e);
        }
        return isSuccess;
    }
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
