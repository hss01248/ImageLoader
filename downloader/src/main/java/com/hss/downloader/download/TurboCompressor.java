package com.hss.downloader.download;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.blankj.utilcode.util.CloseUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.hss01248.media.metadata.ExifUtil;
import com.hss01248.media.metadata.quality.Magick;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.transform.sax.TemplatesHandler;

public class TurboCompressor {

    public static int targetQuality = 80;

    public static boolean compressOriginal(String filePath,int quality){
        File file = new File(filePath);
        File dir = file.getParentFile();
        File file2 = new File(dir, "tmp-"+file.getName());

        boolean compress = TurboCompressor.compressOringinal(file.getAbsolutePath(), quality,file2.getAbsolutePath());
        if(compress) {
            boolean renameTo = file2.renameTo(file); //垃圾api
            if(renameTo){
                //LogUtils.d("rename success:file.exists() "+file.exists()+",file2.exist:"+file2.exists());
                return true;
            }else {
               // LogUtils.d("rename failed "+file.exists()+",file2.exist:"+file2.exists());
                try {
                    boolean copy = FileUtils.copy(file2, file, new FileUtils.OnReplaceListener() {
                        @Override
                        public boolean onReplace(File srcFile, File destFile) {
                            return true;
                        }
                    });
                    if(copy){
                        file2.delete();
                    }else {
                        LogUtils.w("copy failed:1");
                    }
                    return true;
                }catch (Throwable throwable){
                    LogUtils.w("copy failed:2 "+throwable.getClass().getSimpleName()+","+throwable.getMessage());
                    //file2.renameTo(file);
                }
            }


        }else {
            file2.delete();
        }
        return false;
    }
    /**
     *
     * @param srcPath
     * @param quality
     * @param outPath
     * @return 代表是否执行了压缩
     */
    public static boolean compressOringinal(String srcPath,int quality,String outPath){
        File file = new File(srcPath);
        if(!shouldCompress(file,true)){
            LogUtils.d(outPath+",no need to compress");
            return false;
        }

        //todo 过大的图,resize到1600w像素,仿照谷歌.
        Bitmap bitmap = null;
        try {
             bitmap = BitmapFactory.decodeFile(srcPath);
        }catch (Throwable throwable){
            LogUtils.w(srcPath+" , decode bitmap failed:"+throwable.getMessage());
        }

        if(bitmap == null){
            return false;
        }
        boolean success =  false;
        File outFile = new File(outPath);
        try {
            success = compressOringinal2(srcPath,quality,outPath);
        }catch (Throwable throwable){
            throwable.printStackTrace();
            //success = compressByAndroid(bitmap,quality,outPath);
        }

        if(success && outFile.exists() && outFile.length()> 50){
            //如果压缩后的图比压缩前还大,那么就不压缩,返回原图
            if(file.length() < outFile.length()){
                LogUtils.w(outFile.getAbsolutePath()+"  file.length() < outFile.length(), ignore");
                outFile.delete();
                success = false;
            }else {
                success = true;
            }

        }else {
            LogUtils.w(outFile.getAbsolutePath()+"  compress progress fail:",success,outFile.exists(),outFile.length());
            success = false;
        }

        //回写exif信息
        if(success){
            try {
                ExifUtil.writeExif(ExifUtil.readExif(srcPath),outPath);
                return true;
            } catch (Throwable e) {
                LogUtils.w(outPath+",write exif failed:"+e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }


    private static boolean compressOringinal2(String absolutePath, int quality, String outPath) {
        try {
            File file = new File(outPath);
            Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);

            ExifInterface exifInterface = new ExifInterface(absolutePath);
            int oritation = exifInterface.getRotationDegrees();
            Map<String, String> exifMap =  ExifUtil.readExif(absolutePath);;
            if(oritation != 0){
                try {
                    bitmap =   rotaingImageView(oritation,bitmap);
                    exifMap.put(ExifInterface.TAG_ORIENTATION,"0");
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                    LogUtils.w("compress fail when rotate");
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            boolean success =  bitmap.compress(Bitmap.CompressFormat.JPEG,quality,fileOutputStream);
            success =  success && file.exists() && file.length() > 50;
            CloseUtils.closeIO(fileOutputStream);

            if(success){
                try {
                    if(exifMap == null){
                        ExifUtil.writeExif(exifMap,outPath);
                    }
                    return true;
                } catch (Throwable e) {
                    LogUtils.w(outPath+",write exif failed:"+e.getMessage());
                    e.printStackTrace();
                }
            }
            return false;

        } catch (Throwable e) {
            LogUtils.w(outPath,"compress fail by e",e.getClass().getSimpleName(),e.getMessage());
            return false;
        }
    }

    //旋转图片

     static Bitmap rotaingImageView(int degree, Bitmap bitmap) { //angle 旋转的角度  bitmap需要旋转的图片

            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return resizedBitmap;
    }

    static boolean shouldCompress(File pathname,boolean checkQuality) {
        if(!pathname.exists()){
            LogUtils.w("source file not exist: "+pathname);
            return false;
        }

        String name = pathname.getName();
        int idx = name.lastIndexOf(".");
        if(idx <0 || idx >= name.length()-1){
            return false;
        }
        String suffix = name.substring(idx+1);
        boolean isJpg = suffix.equalsIgnoreCase("jpg")
                || suffix.equalsIgnoreCase("jpeg");
        if(!isJpg){
            if(suffix.equalsIgnoreCase("png")){
                return true;
            }
            if(suffix.equalsIgnoreCase("gif") || suffix.equalsIgnoreCase("webp")){
                return false;
            }
            return false;
        }
        if(!checkQuality){
            return true;
        }

        int quality = getQuality(pathname.getAbsolutePath());
        //LogUtils.d("quality:"+quality +":"+pathname.getAbsolutePath());
        return  (quality ==  0) ||  (quality > getQuality());
    }

    static int getQuality() {
        return targetQuality;
    }

    static int getQuality(String path) {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        File file = new File(path);
        if (!file.exists()) {
            return 0;
        }
        FileInputStream inputStream  = null;
        try {
            inputStream = new FileInputStream(file);
            return new Magick().getJPEGImageQuality(inputStream);
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
