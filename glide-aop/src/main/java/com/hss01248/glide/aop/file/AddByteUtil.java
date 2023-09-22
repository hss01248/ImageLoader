package com.hss01248.glide.aop.file;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
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
    static  boolean useFileChannel = false;
    //useFileChannel = true: 2011ms  false:1317ms
    public static final String fileSuffix = ".3";

    public static String addByte(String filePath){

        long startTime = System.currentTimeMillis();

        try {
            // 打开文件
            File file = new File(filePath);
            long originalFileSize = file.length();
            long newFileSize = 0 ;
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            // 读取文件的第一个字节
            int firstByte = raf.read();
            if(dataToAdd == firstByte){
                raf.close();
               LogUtils.i("已经是加密文件了: "+ file.getAbsolutePath());
                return filePath;
            }
            if(useFileChannel){
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
            }else {
                raf.close();
                byte[] bytes = new byte[]{(byte) dataToAdd};
                //放到内部cache目录:
                File file1 = new File(DirOperationUtil.getTmpDir(),file.getName()+fileSuffix);
                filePath = file1.getAbsolutePath();
                if(!file1.exists()){
                    file1.createNewFile();
                }
                InputStream inputStream = new FileInputStream(file);
                ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes);
                SequenceInputStream newInputStream = new SequenceInputStream(inputStream1,inputStream);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file1));

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = newInputStream.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                bos.flush();

                newInputStream.close();
                bos.close();
                //会触发系统拦截,所以要覆盖原文件,不要删除.
                //file.delete();
                newFileSize = file1.length();
                boolean copy = FileUtils.copy(file1, file, new FileUtils.OnReplaceListener() {
                    @Override
                    public boolean onReplace(File srcFile, File destFile) {
                        return true;
                    }
                });
                if(copy){
                    file1.delete();
                    filePath = file.getAbsolutePath();
                }else {
                    LogUtils.w("文件拷贝失败",file1);
                }

            }

            LogUtils.d("添加字节成功, cost: "+(System.currentTimeMillis() - startTime)
                    +"ms, original file size : "+originalFileSize+",new size: "+newFileSize+", path: "+filePath);
            return filePath;
        } catch (Throwable e) {
            LogUtils.w(e);
            return filePath;
        }
    }

    public static File createTmpOriginalFile(String sourceFilePath){
        return createTmpOriginalFile(DirOperationUtil.getTmpDir(),sourceFilePath);
    }
    public static File createTmpOriginalFile(File tmpDir,String sourceFilePath){

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
        if(tmpDir == null){
            tmpDir = file.getParentFile();
        }
        File destinationFile0 = new File(tmpDir,"tmp-"+file.getName());
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
                    +"ms, filesize: "+ sourceFileSize+"B");
            return destinationFile0;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

}
