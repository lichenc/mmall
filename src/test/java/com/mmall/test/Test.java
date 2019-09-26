package com.mmall.test;

import java.io.File;
import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        String path = "C:\\isprint\\new\\newFoder";
        File foder = new File(path);//根据文件路径创建实例
        if(!foder.exists())
        {
            foder.setWritable(true);//设置可写权限
            foder.mkdirs();//创建目录
        }
        String qrFileName = "abc.txt";
        File targetFile = new File(path ,qrFileName);
        //测试此抽象路径名表示的文件是否是一个标准文件
        //这里是判断在指定文件是否已经存在
        System.out.println(targetFile.getName());
        /**
         *  abc.txt
         */
       /* if(!targetFile.isFile())
        {
            targetFile.createNewFile();
        }*/

    }
}
