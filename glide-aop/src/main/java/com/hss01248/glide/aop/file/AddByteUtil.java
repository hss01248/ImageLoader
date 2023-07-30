package com.hss01248.glide.aop.file;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Despciption todo
 * @Author hss
 * @Date 25/07/2023 09:28
 * @Version 1.0
 */
public class AddByteUtil {
    // 要添加的字节数据
    static  byte dataToAdd = 0x66;

    public static String addByte(String filePath){

        long startTime = System.currentTimeMillis();

        try {
            // 打开文件
            File file = new File(filePath);
            long originalFileSize = file.length();
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            // 读取文件的第一个字节
            int firstByte = raf.read();
            if(dataToAdd == firstByte){
                raf.close();
                System.out.println("已经是加密文件了: "+ file.getAbsolutePath());
                return filePath;
            }
            raf.seek(0);
            FileChannel channel = raf.getChannel();
            // 读取原始数据
            ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
            channel.read(buffer);
            buffer.flip();
            // 创建新的ByteBuffer来添加数据
            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + 1);
            // 在开头添加一个字节
            newBuffer.put(dataToAdd);
            // 将原始数据写入新的ByteBuffer
            newBuffer.put(buffer);
            // 切换新的ByteBuffer为读模式
            newBuffer.flip();
            // 清空文件内容
            channel.truncate(0);
            // 将新的ByteBuffer写入文件
            channel.write(newBuffer);
            // 关闭通道和文件
            channel.close();
            raf.close();
            System.out.println("添加字节成功, cost: "+(System.currentTimeMillis() - startTime)
                    +"ms, filesize: "+ file.length()+"b,original file size : "+originalFileSize+",new size: "+file.length());
        return filePath;
        } catch (Throwable e) {
            e.printStackTrace();
            return filePath;
        }
    }

    public static File createTmpOriginalFile(String sourceFilePath){

        File file = new File(sourceFilePath);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int read = inputStream.read();
            inputStream.close();
            if(read != dataToAdd){
                System.out.println("不是加密文件 " +"sourceFilePath : "+ file.getAbsolutePath());
                return file;
            }
        } catch (Exception e) {
           e.printStackTrace();
           return file;
        }
        File destinationFile0 = new File(file.getParentFile(),"tmp-"+file.getName());
        System.out.println("文件信息 " +"sourceFilePath filesize: "+ file.length()+",destinationFile size: "+destinationFile0.length());
        if(destinationFile0.exists()  && destinationFile0.length() == file.length()-1){
            System.out.println("临时解密文件已经存在: "+ destinationFile0.getAbsolutePath());
            return destinationFile0;
        }
        long startTime = System.currentTimeMillis();
        try (RandomAccessFile sourceFile = new RandomAccessFile(sourceFilePath, "r");
             RandomAccessFile destinationFile = new RandomAccessFile(destinationFile0, "rw")) {

            // 获取源文件和目标文件的FileChannel
            FileChannel sourceChannel = sourceFile.getChannel();
            FileChannel destinationChannel = destinationFile.getChannel();

            // 复制文件（不包括开始的字节）
            long sourceFileSize = sourceChannel.size();
            sourceChannel.position(1);
            // 跳过开始的字节
            destinationChannel.transferFrom(sourceChannel, 0, sourceFileSize - 1);

            System.out.println("文件已成功复制并移除了开始的字节,cost: "+(System.currentTimeMillis() - startTime)
                    +"ms, filesize: "+ sourceFileSize/1024+"kB");
            return destinationFile0;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
