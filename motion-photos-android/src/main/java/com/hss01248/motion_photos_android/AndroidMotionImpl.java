package com.hss01248.motion_photos_android;

import android.media.MediaMetadataRetriever;
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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    @Override
    public Map<String, Object> metaOfImage(String fileOrUriPath) {
        InputStream stream = null;
        try {
            stream =   steam(fileOrUriPath);
            if(stream ==null){
                return null;
            }
            Map<String, Object> map = new TreeMap<>();
            ExifInterface exifInterface = new ExifInterface(stream);
            Field[] fields = ExifInterface.class.getDeclaredFields();
            for (Field field : fields) {
                if(field.getName().startsWith("TAG_")){
                    field.setAccessible(true);
                    String  str = field.get(ExifInterface.class)+"";
                    map.put(str,exifInterface.getAttribute(str));

                }
            }
            return map;
        }catch (Throwable throwable){
            throwable.printStackTrace();
            return null;
        }finally {
            if(stream !=null){
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Map<String, Object> metaOfVideo(String fileOrUriPath) {

        try {
            Map<String, Object> map = new TreeMap<>();
            MediaMetadataRetriever exifInterface = new MediaMetadataRetriever();
            exifInterface.setDataSource(fileOrUriPath);
            Field[] fields = MediaMetadataRetriever.class.getDeclaredFields();
            for (Field field : fields) {
                if(field.getName().startsWith("METADATA_KEY_")){
                    field.setAccessible(true);
                    int   str = (int) field.get(ExifInterface.class);
                    map.put(field.getName().substring("METADATA_KEY_".length()).toLowerCase(),exifInterface.extractMetadata(str));
                }
            }
            return map;
        }catch (Throwable throwable){
            throwable.printStackTrace();
            return null;
        }
    }

    public static File compressMp4File(String fileOrUriPath){

        File dir = new File(Utils.getApp().getExternalCacheDir(),"motion-videos") ;
        File file = new File(fileOrUriPath);
        final File[] file2 = {new File(dir, "out-" + file.getName())};

        CountDownLatch latch = new CountDownLatch(1);
        VideoCompressUtil.doCompress(false,fileOrUriPath, dir.getAbsolutePath(), CompressType.TYPE_UPLOAD_1080P,
                new ICompressListener() {
                    @Override
                    public void onFinish(String outputFilePath) {
                        LogUtils.d("compress finished: ",outputFilePath, file2[0].getAbsolutePath());
                        file2[0] = new File(outputFilePath);
                        if(file.length() <= file2[0].length()){
                            LogUtils.w("压缩后文件变大",fileOrUriPath,outputFilePath);
                            file2[0] = file;
                        }

                        latch.countDown();
                    }

                    @Override
                    public void onError(String message) {
                        LogUtils.d("compress failed: ",message);
                        latch.countDown();
                    }
                });
        try {
            boolean await = latch.await(6, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LogUtils.w(e,fileOrUriPath);
        }
        LogUtils.d("compress return: ", file2[0].getAbsolutePath());
        return file2[0];

    }
}
