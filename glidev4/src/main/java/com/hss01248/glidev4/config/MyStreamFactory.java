package com.hss01248.glidev4.config;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.load.model.FileLoader;
import com.hss01248.glide.aop.net.ModifyResponseBodyInterceptor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

/**
 * @Despciption todo
 * @Author hss
 * @Date 28/07/2023 11:48
 * @Version 1.0
 */
public class MyStreamFactory extends FileLoader.Factory<InputStream> {
    public MyStreamFactory() {
        super(
                new FileLoader.FileOpener<InputStream>() {
                    @Override
                    public InputStream open(File file) throws FileNotFoundException {
                        InputStream inputStream = new FileInputStream(file);
                        int read = 0;
                        try {
                            read = inputStream.read();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (read == ModifyResponseBodyInterceptor.dataToAdd) {
                            //隐藏文件,那么要去掉第一个字符
                            try {
                                LogUtils.i("隐藏文件,那么要去掉第一个字符-->inputStream.available():" + inputStream.available() + ", " + file.getAbsolutePath());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return inputStream;
                        } else {

                            try {
                                LogUtils.i("非隐藏文件,正常使用-->,inputStream.reset():"+inputStream.available()+", "+file.getAbsolutePath());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            //inputStream.reset();//这里会导致死循环. 如何处理?

                            byte[] bytes = new byte[]{(byte) read};
                            ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes);
                            SequenceInputStream newInputStream = new SequenceInputStream(inputStream1, inputStream);
                            return  newInputStream;
                        }
                    }

                    @Override
                    public void close(InputStream inputStream) throws IOException {
                        inputStream.close();
                    }

                    @Override
                    public Class<InputStream> getDataClass() {
                        return InputStream.class;
                    }
                });
    }
}
