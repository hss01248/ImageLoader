package com.hss01248.motion_photos_android;

import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.motion_photos.IMotion;
import com.hss01248.videocompress.CompressType;
import com.hss01248.videocompress.VideoCompressUtil;
import com.hss01248.videocompress.listener.ICompressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

/**
 * @Despciption todo
 * @Author hss
 * @Date 9/18/24 11:41 AM
 * @Version 1.0
 */
public class AndroidMotionImpl implements IMotion {
    @Override
    public long length(String fileOrUriPath) throws Throwable {
        InputStream stream = null;
        try {
            stream =   steam(fileOrUriPath);
            if(stream ==null){
                return 0;
            }
            return stream.available();
        }catch (Throwable throwable){
            throwable.printStackTrace();
            return 0;
        }finally {
            if(stream !=null){
                stream.close();
            }
        }
    }

    @Override
    public String readXmp(String fileOrUriPath) throws Throwable{
        InputStream stream = null;
        try {
            stream =   steam(fileOrUriPath);
            if(stream ==null){
                return null;
            }
            ExifInterface exifInterface = new ExifInterface(stream);
            return exifInterface.getAttribute(ExifInterface.TAG_XMP);
        }catch (Throwable throwable){
            throwable.printStackTrace();
            return null;
        }finally {
            if(stream !=null){
                stream.close();
            }
        }
    }

    InputStream steam(String fileOrUriPath) throws Throwable{
        if(fileOrUriPath.startsWith("content://")){
            Uri uri = Uri.parse(fileOrUriPath);
            return Utils.getApp().getContentResolver().openInputStream(uri);
        }else if(fileOrUriPath.startsWith("file://")){
            Uri uri = Uri.parse(fileOrUriPath);
            File file = new File(uri.getPath());
            if(file.exists() && file.length() >0){
                return new FileInputStream(file);
            }
        }else if(fileOrUriPath.startsWith("http://") || fileOrUriPath.startsWith("https://")){
            //下载到本地....

        }else {
            File file = new File(fileOrUriPath);
            if(file.exists() && file.length() >0){
                return new FileInputStream(file);
            }
        }
        return null;
    }

    @Override
    public String mp4CacheFile(String path) {
       File dir = new File(Utils.getApp().getExternalCacheDir(),"motion-videos") ;
       if(!dir.exists()){
           dir.mkdirs();
       }
       File file = new File(path);
       File file2 = new File(dir,file.getName()+".mp4");
        return file2.getAbsolutePath();
    }

    public static File compressMp4File(String fileOrUriPath){

        File dir = new File(Utils.getApp().getExternalCacheDir(),"motion-videos") ;
        File file = new File(fileOrUriPath);
        File file2 = new File(dir,"out-"+file.getName());
        CountDownLatch latch = new CountDownLatch(1);
        VideoCompressUtil.doCompressAsync(fileOrUriPath, dir.getAbsolutePath(), CompressType.TYPE_UPLOAD_720P,
                new ICompressListener() {
                    @Override
                    public void onFinish(String outputFilePath) {
                        latch.countDown();
                    }

                    @Override
                    public void onError(String message) {
                        latch.countDown();
                    }
                });
        try {
            latch.await();
        } catch (InterruptedException e) {
            LogUtils.w(e);
        }
        return file2;

    }
}
