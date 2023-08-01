package com.hss01248.glide.aop.file;

import com.hss01248.glide.aop.net.ModifyResponseBodyInterceptor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;

/**
 * @Despciption todo
 * @Author hss
 * @Date 01/08/2023 09:37
 * @Version 1.0
 */
public class ReadFileUtil {

    public static InputStream read(File file) throws  Throwable{
        FileInputStream inputStream = null;
        inputStream = new FileInputStream(file);
        int read = inputStream.read();
        if(read == ModifyResponseBodyInterceptor.dataToAdd){
            //从第二个字节开始读
           return inputStream;
        }else {
            System.out.println("不是加密文件 " +"sourceFilePath : "+ file.getAbsolutePath());
            inputStream.close();
            return new FileInputStream(file);
        }
    }

    public static InputStream read(InputStream inputStream) throws Throwable {
        if(inputStream == null || inputStream.available()<=0){
            return inputStream;
        }
        int read = inputStream.read();
        if(read == ModifyResponseBodyInterceptor.dataToAdd){
            //从第二个字节开始读
            return inputStream;
        }else {
            byte[] bytes = new byte[]{(byte) read};
            ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes);
            SequenceInputStream newInputStream = new SequenceInputStream(inputStream1,inputStream);
            //注意: SequenceInputStream的available()的数字是不准的
            return  newInputStream;
        }
    }
}
