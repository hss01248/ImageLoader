package com.hss01248.glide.aop.file;

import android.graphics.BitmapRegionDecoder;

import com.blankj.utilcode.util.LogUtils;
import com.hss01248.glide.aop.net.ModifyResponseBodyInterceptor;
import com.hss01248.logforaop.LogMethodAspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

@Aspect
public class GlideLoadFileAspect {

    //@Around("execution(* com.bumptech.glide.load.model.FileLoader.buildLoadData(..))")
    public Object buildLoadData(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        File file = (File) args[0];
        File file2 = mapFile(file);
        LogUtils.w("original file: "+file.getAbsolutePath()+"\n map file: "+ file2.getAbsolutePath());
        args[0] = file2;
        new Throwable("test").printStackTrace();
        return joinPoint.proceed(args);
    }

    private File mapFile(File file) {
        return AddByteUtil.createTmpOriginalFile(file.getAbsolutePath());
    }

    @Around("execution(* com.shizhefei.view.largeimage.factory.FileBitmapDecoderFactory.made())")
    public Object fileBitmapDecoderFactory(ProceedingJoinPoint joinPoint) throws Throwable {
        // return BitmapRegionDecoder.newInstance(path, false);
        return joinPoint.proceed(joinPoint.getArgs());
    }

    @Around("execution(* com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory.made())")
    public Object inputStreamBitmapDecoderFactory(ProceedingJoinPoint joinPoint) throws Throwable {
        //return BitmapRegionDecoder.newInstance(inputStream, false);
        return joinPoint.proceed(joinPoint.getArgs());
    }

    @Around("execution(* pl.droidsonroids.gif.InputSource.FileSource.open())")
    public Object largeGif(ProceedingJoinPoint joinPoint) throws Throwable {
        //return new GifInfoHandle(mPath); 改成stream
        return joinPoint.proceed(joinPoint.getArgs());
    }

    @Around("execution(* com.bumptech.glide.util.ByteBufferUtil.fromFile(..))")
    public Object fromFile(ProceedingJoinPoint joinPoint) throws Throwable {

        File file = (File) joinPoint.getArgs()[0];

        RandomAccessFile raf = null;
        FileChannel channel = null;
        try {
            long fileLength = file.length();
            // See #2240.
            if (fileLength > Integer.MAX_VALUE) {
                throw new IOException("File too large to map into memory");
            }
            // See b/67710449.
            if (fileLength == 0) {
                throw new IOException("File unsuitable for memory mapping");
            }

            raf = new RandomAccessFile(file, "r");
            channel = raf.getChannel();


            try {
                FileInputStream inputStream = new FileInputStream(file);
                int read = inputStream.read();
                inputStream.close();

                if(read != ModifyResponseBodyInterceptor.dataToAdd){
                    System.out.println("不是加密文件 " +"sourceFilePath : "+ file.getAbsolutePath());
                    return channel.map(FileChannel.MapMode.READ_ONLY, 0, fileLength).load();
                }else {
                    System.out.println("--->是加密文件,从第2个字节开始读 " +"sourceFilePath : "+ file.getAbsolutePath());
                    return channel.map(FileChannel.MapMode.READ_ONLY, 1, fileLength-1).load();
                }
            } catch (Exception e) {
                throw  e;
            }
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    // Ignored.
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    // Ignored.
                }
            }
        }

    }
}
