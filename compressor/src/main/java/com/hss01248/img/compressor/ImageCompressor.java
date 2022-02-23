package com.hss01248.img.compressor;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;

import androidx.exifinterface.media.ExifInterface;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.CloseUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.avif.AvifEncoder;
import com.hss01248.fileoperation.FileDeleteUtil;
import com.hss01248.media.metadata.ExifUtil;
import com.hss01248.media.metadata.quality.Magick;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;


/**
 * 主要用于单图压缩
 * 默认不使用avif.
 * 可以配置使用avif
 */
public class ImageCompressor {

    public static int targetJpgQuality = 80;
    public static boolean compressToAvif = false;

    public static File compress(String filePath, boolean deleteOriginalIfAvifSuccess, boolean noAvifOver2k){
        AvifEncoder.init(Utils.getApp());
        File in = new File(filePath);
        //太大的图,使用jpg,不使用avif. 否则压缩,解析都太过耗时.
        if(!in.exists()){
            return in;
        }
        if(!isImagesToCompress(filePath)){
            return in;
        }
        if(!compressToAvif){
            return compressOriginalToJpg(filePath,targetJpgQuality,deleteOriginalIfAvifSuccess);
        }
        if(noAvifOver2k){
            int[] wh = getImageWidthHeight(filePath);
            if(wh[0] * wh[1] > 10000000){
                //一千万像素以上,则不使用avif,使用jpg压缩
                //2k
                LogUtils.i("分辨率大于2k,使用jpg压缩,不使用avif,避免过多耗时,以及大图查看的不便: ",filePath);
                File out =  compressOriginalToJpg(filePath,targetJpgQuality,deleteOriginalIfAvifSuccess);
                return out;
            }
        }
        File file = AvifEncoder.encodeOneFile(filePath);

        if(file.getAbsolutePath().equals(filePath)){
            //没有压缩. 否则后缀名变了
            File out =  compressOriginalToJpg(filePath,targetJpgQuality,deleteOriginalIfAvifSuccess);
            return out;
        }else {
           //删除jpg/png原图
            if(deleteOriginalIfAvifSuccess){
                deleteFile(in);
            }
            return file;
        }
    }

   public static void deleteFile(File file){
       FileDeleteUtil.deleteImage(file.getAbsolutePath(), false, new Observer<Boolean>() {
           @Override
           public void onSubscribe(@NonNull Disposable d) {

           }

           @Override
           public void onNext(@NonNull Boolean aBoolean) {
            LogUtils.i("文件删除结果",aBoolean,file.getAbsolutePath());
           }

           @Override
           public void onError(@NonNull Throwable e) {
               LogUtils.i("文件删除结果",e);
           }

           @Override
           public void onComplete() {

           }
       });
    }

    /**
     * 非常耗时
     * @param path
     * @return
     */
    static int[] getImageWidthHeight(String path) {
        if(path.endsWith(".avif") || !isImagesToCompress(path)){
            return new int[]{-1,-1};
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{options.outWidth, options.outHeight};
    }

     static boolean isImagesToCompress(String path) {
        return path.endsWith(".jpg")
                || path.endsWith(".jpeg")
        || path.endsWith(".JPG")
                || path.endsWith(".JPEG")
        || path.endsWith(".png")
                || path.endsWith(".PNG")
                || path.endsWith(".webp")
                || path.endsWith(".WEBP");
    }

    public interface Callback{
        void onResult(File file,boolean hasCompressed);

       default void onFailed(Throwable throwable){
           throwable.printStackTrace();
       }
    }





    public static void compressAsync(String filePath, boolean deleteOriginalIfSuccessAndSuffixChanged, boolean noAvifOver2k, boolean withLoadingDialog, Callback callback){
        ProgressDialog dialog = null;
        if(withLoadingDialog){
            dialog = new ProgressDialog(ActivityUtils.getTopActivity());
            dialog.setMessage("图片压缩中...");
        }
        ProgressDialog finalDialog = dialog;

        ThreadUtils.Task<File> task = new ThreadUtils.Task<File>() {
            @Override
            public File doInBackground() throws Throwable {
                File file = compress(filePath, deleteOriginalIfSuccessAndSuffixChanged,noAvifOver2k);
                return file;
            }

            @Override
            public void onSuccess(File result) {
                if(finalDialog != null){
                    finalDialog.dismiss();
                }
                boolean notCompressed = filePath.equals(result.getAbsolutePath()) && result.length() == new File(filePath).length();
                callback.onResult(result,!notCompressed);


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                if(finalDialog != null){
                    finalDialog.dismiss();
                }
                callback.onFailed(t);

            }
        };
        if(compressToAvif){
            //avif只能单线程压缩
            ThreadUtils.executeBySingle(task);
        }else {
            ThreadUtils.executeByIo(task);
        }

    }



    public static File compressOriginalToJpg(String filePath, int quality, boolean deleteOriginalIfNotSameSuffix){
        File file = new File(filePath);
        File dir = file.getParentFile();
        //区分png,jpg
        String name = file.getName();
        boolean sameSuffix = false;
        if(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".JPG") || name.endsWith(".JPEG")){
            sameSuffix = true;
        }else {
            name = name+".jpg";
        }
        File file2 = null;
        if(sameSuffix){
             file2 = new File(dir, "tmp-"+file.getName());
        }else {
             file2 = new File(dir, name);
        }


        boolean compress = ImageCompressor.compressOringinal(file.getAbsolutePath(), quality,file2.getAbsolutePath());
        if(compress) {
            if(sameSuffix){
                boolean renameTo = file2.renameTo(file); //垃圾api,只能同扩展名处理. 自动删除file2,变成file1. 等效于
                if(!renameTo){
                    LogUtils.w("同jpg扩展名时,renameTo失败,使用fileCopy: "+ file2);
                    try {
                        boolean copy = FileUtils.copy(file2, file, new FileUtils.OnReplaceListener() {
                            @Override
                            public boolean onReplace(File srcFile, File destFile) {
                                return true;
                            }
                        });
                        if(copy){
                            deleteFile(file2);
                            return file;
                        }else {
                            LogUtils.w("同jpg扩展名时,renameTo失败,fileCopy也失败,则使用带tmp的文件作为最终文件: "+file2);
                            return file2;
                        }
                    }catch (Throwable throwable){
                        LogUtils.w("copy failed:2 "+throwable.getClass().getSimpleName()+","+throwable.getMessage());
                        LogUtils.w("同jpg扩展名时,renameTo失败,fileCopy也失败,还tm抛异常, 则使用带tmp的文件作为最终文件: "+file2);
                        return file2;
                    }
                }else {
                    //成功,且文件名不变.
                    return file;
                }
            }else {
                //扩展名变了,返回新路径,并确认是否要删除原文件
                LogUtils.w("扩展名变了,返回新路径,并确认是否要删除原文件: "+ file2);
                if(deleteOriginalIfNotSameSuffix){
                    deleteFile(file);
                }
                return file2;
            }
        }else {
            //LogUtils.d("文件无需压缩或压缩失败,则返回原文件: "+ file);
            deleteFile(file2);
            return file;
        }
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
                deleteFile(outFile);
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
            } catch (Throwable e) {
                LogUtils.w(outPath+",write exif failed:"+e.getMessage());
                e.printStackTrace();
            }
            return true;
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
        return targetJpgQuality;
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
