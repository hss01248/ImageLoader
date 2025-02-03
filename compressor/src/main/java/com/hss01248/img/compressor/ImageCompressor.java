package com.hss01248.img.compressor;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.exifinterface.media.ExifInterface;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.CloseUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.fileoperation.FileDeleteUtil;
import com.hss01248.media.metadata.ExifUtil;
import com.hss01248.media.metadata.FileTypeUtil;
import com.hss01248.media.metadata.quality.Magick;
import com.hss01248.motion_photos.MotionPhotoUtil;
import com.hss01248.motion_photos_android.AndroidMotionImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
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

    public static int targetJpgQuality = 85;
    public static boolean compressToAvif = false;
    public static boolean compressToWebp = false;
    public static int targetWebpQuality = 75;

    public static boolean doNotCompressMotionPhoto = false;

    public static File compress(String filePath, boolean deleteOriginalIfAvifSuccess, boolean noAvifOver2k) {
        //AvifEncoder.init(Utils.getApp());
        File in = new File(filePath);
        //太大的图,使用jpg,不使用avif. 否则压缩,解析都太过耗时.
        if (!in.exists()) {
            return in;
        }
        if (!isImagesToCompress(filePath, targetJpgQuality)) {
            LogUtils.v("无需压缩", filePath);
            return in;
        }
        //jpg质量判断

        if (!compressToAvif) {
            return compressOriginalToJpg(filePath, targetJpgQuality);
        }
        if (noAvifOver2k) {
            int[] wh = getImageWidthHeight(filePath);
            if (wh[0] * wh[1] > 10000000) {
                //一千万像素以上,则不使用avif,使用jpg压缩
                //2k
                LogUtils.i("分辨率大于2k,使用jpg压缩,不使用avif,避免过多耗时,以及大图查看的不便: ", filePath);
                File out = compressOriginalToJpg(filePath, targetJpgQuality);
                return out;
            }
        }
       /* File file = AvifEncoder.encodeOneFile(filePath);

        if (file.getAbsolutePath().equals(filePath)) {
            //没有压缩. 否则后缀名变了
            File out = compressOriginalToJpg(filePath, targetJpgQuality);
            return out;
        } else {
            //删除jpg/png原图
            if (deleteOriginalIfAvifSuccess) {
                deleteFile(in);
            }
            return file;
        }*/
        return new File(filePath);
    }

    public static void deleteFile(File file) {
        if (file.getAbsolutePath().contains(Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getParentFile().getAbsolutePath())) {
            file.delete();
            return;
        }
        FileDeleteUtil.deleteImage(file.getAbsolutePath(), false, new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {
                LogUtils.i("文件删除结果", aBoolean, file.getAbsolutePath());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                LogUtils.i("文件删除结果", e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 非常耗时
     *
     * @param path
     * @return
     */
    static int[] getImageWidthHeight(String path) {
        if (path.endsWith(".avif") || !isImagesToCompress(path, targetJpgQuality)) {
            return new int[]{-1, -1};
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

    public static boolean shouldCompress(File pathname, boolean checkQuality) {
        return isImagesToCompress(pathname.getAbsolutePath(), getQuality());
    }

    public static boolean isImagesToCompress(String path, int targetJpgQuality) {
        boolean isImage = path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".JPG")
                || path.endsWith(".JPEG")
                || path.endsWith(".png")
                || path.endsWith(".PNG");
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!isImage) {
            return false;
        }
        String type = FileTypeUtil.getType(file);
        if ("jpg".equals(type)) {
            try {
                if(isPanoramaImage(file.getAbsolutePath())){
                    return false;
                }
                if(doNotCompressMotionPhoto){
                    if(MotionPhotoUtil.isMotionImage(file.getAbsolutePath(),false)){
                        LogUtils.v("MotionImage 全局配置了不压缩", path);
                        return false;
                    }
                }
                FileInputStream inputStream = new FileInputStream(file);
                int quality = new Magick().getJPEGImageQuality(inputStream);
                CloseUtils.closeIO(inputStream);
                LogUtils.v("jpg压缩判断", "预期:" + targetJpgQuality + ",实际:" + quality, path);
                if (quality == 0) {
                    //大概率是mezjpeg压缩的-比如头条的图片. 不再压缩
                    return false;
                } else {
                    if (quality > targetJpgQuality) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return true;
            }
        } else {
            return true;
        }

        // || path.endsWith(".webp")
        //  || path.endsWith(".tif")
        //                || path.endsWith(".WEBP")
    }

    public static boolean isPanoramaImage(String path){
        Map<String, String> map = ExifUtil.readExif(path);
        String xml = map.get("Xmp");
        if(!TextUtils.isEmpty(xml) ){
            if(xml.contains("GPano:UsePanoramaViewer")){
                LogUtils.i("根据exif特征识别出为360全景图,不进行压缩");
                return true;
            }
            /*if(xml.contains("MotionPhoto")){
                LogUtils.i("根据exif特征识别出为MotionPhoto,不进行压缩");
                return true;
            }*/
            //MotionPhoto
        }
        return false;
    }

    public interface Callback {
        void onResult(File file, boolean hasCompressed);

        default void onFailed(Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    public static void compressAsync(String filePath, boolean deleteOriginalIfSuccessAndSuffixChanged, boolean noAvifOver2k, boolean withLoadingDialog, Callback callback) {
        ProgressDialog dialog = null;
        if (withLoadingDialog) {
            dialog = new ProgressDialog(ActivityUtils.getTopActivity());
            dialog.setMessage("图片压缩中...");
        }
        ProgressDialog finalDialog = dialog;

        long originalSize = new File(filePath).length();
        ThreadUtils.Task<File> task = new ThreadUtils.Task<File>() {
            @Override
            public File doInBackground() throws Throwable {
                File file = compress(filePath, deleteOriginalIfSuccessAndSuffixChanged, noAvifOver2k);
                return file;
            }

            @Override
            public void onSuccess(File result) {
                if (finalDialog != null) {
                    finalDialog.dismiss();
                }
                //filePath.equals(result.getAbsolutePath()) &&
                boolean notCompressed = result.length() == originalSize;
                callback.onResult(result, !notCompressed);


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                if (finalDialog != null) {
                    finalDialog.dismiss();
                }
                callback.onFailed(t);

            }
        };
        if (compressToAvif) {
            //avif只能单线程压缩
            ThreadUtils.executeBySingle(task);
        } else {
            ThreadUtils.executeByIo(task);
        }

    }


    /**
     * 策略: png压缩为jpg,不更改后缀名
     *
     * @param filePath
     * @param quality
     * @return
     */
    public static File compressOriginalToJpg(String filePath, int quality) {
        synchronized (filePath) {
            File file = new File(filePath);
            File dir = Utils.getApp().getExternalFilesDir("compressTmp");
            //区分png,jpg
            String name = file.getName();
            boolean sameSuffix = false;
       /* if(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".JPG") || name.endsWith(".JPEG")){
            sameSuffix = true;

        }else {
            name = name+".jpg";
        }*/
            name = name + ".tmp";
            File file2 = null;
            //todo 要采用应用私有目录为工作目录,避免权限问题,以及miui警告的图片删除问题
            //todo 还要采用非图片后缀名,避免miui警告问题. miui他妈的管的真多
            if (sameSuffix) {
                file2 = new File(dir, file.getName());
            } else {
                //file2 = new File(dir, name);
                file2 = new File(dir, name);
            }


            boolean compress = ImageCompressor.compressOringinal(file.getAbsolutePath(), quality, file2.getAbsolutePath());
            if (compress) {
                //todo renameTo - 垃圾api,只能同扩展名处理. 自动删除file2,变成file1.
         /*   boolean renameTo = file2.renameTo(file);
            if(!renameTo){*/
                LogUtils.i("不同jpg扩展名时,不能renameTo,使用fileCopy: " + file2);
                try {
                    //todo 文件覆盖也会被miui警告,去tmd-->因为内部调用了file.delete()
                  /*  boolean copy = FileUtils.copy(file2, file, new FileUtils.OnReplaceListener() {
                        @Override
                        public boolean onReplace(File srcFile, File destFile) {
                            return true;
                        }
           w        });*/
                    File targetFile = file;
                    if(compressToWebp){
                        targetFile = new File(file.getParentFile(),file.getName().substring(0,file.getName().lastIndexOf("."))+".webp");
                    }
                    boolean copy = FileIOUtils.writeFileFromIS(targetFile, new FileInputStream(file2));
                    if (copy) {
                        deleteFile(file2);
                        if(targetFile != file){
                            //return targetFile;
                            LogUtils.d("fileCopy成功,删除原文件: " + file.getAbsolutePath());
                            //deleteFile(file);
                        }
                        //如果是mediastore的图,就更新它在mediastore中的大小:
                        updateMediaStore(targetFile.getAbsolutePath());
                        return targetFile;
                    } else {
                        deleteFile(file2);
                        LogUtils.i("fileCopy也失败,则使用原文件: " + file2);
                        return file;
                    }
                } catch (Throwable throwable) {
                    deleteFile(file2);
                    LogUtils.w("copy failed:2 " + throwable.getClass().getSimpleName() + "," + throwable.getMessage());
                    LogUtils.w("fileCopy也失败,还tm抛异常, 则使用原文件作为最终文件: " + file2);
                    return file;
                }
           /* }else {
                //成功,且文件名不变.
                return file;
            }*/
            } else {
                LogUtils.d("文件无需压缩或压缩失败,则返回原文件: " + file);
                deleteFile(file2);
                return file;
            }
        }

    }

    private static void updateMediaStore(String absolutePath) {
        scanFile(Utils.getApp(),absolutePath);
    }

    public static void scanFile(Context context, String filePath) {
        MediaScannerConnection.scanFile(context, new String[]{filePath}, null,
                new MediaScannerConnection.MediaScannerConnectionClient() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        if (uri != null) {
                            System.out.println("Scan completed, Uri: " + uri.toString());
                        } else {
                            System.out.println("Scan failed for path: " + path);
                        }
                    }

                    @Override
                    public void onMediaScannerConnected() {
                        // Not used, but required by interface
                    }
                });
    }
    public static void updateFileSize(Context context, String filePath) {
        // 获取 ContentResolver
        ContentResolver contentResolver = context.getContentResolver();

        // 构造文件的 Uri
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // 查询文件在 MediaStore 中的 _id
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = {filePath};

        Cursor cursor = contentResolver.query(imageUri, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            // 获取文件的 _id
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            cursor.close();

            // 构造要更新的 Uri
            Uri updateUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

            // 获取文件的实际大小
            File file = new File(filePath);
            long fileSize = file.length();

            // 准备更新的数据
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.SIZE, fileSize);
            // 执行更新
            int rowsUpdated = contentResolver.update(updateUri, contentValues, null, null);
            if (rowsUpdated > 0) {
                System.out.println("File size updated successfully!");
            } else {
                System.out.println("Failed to update file size.");
            }
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * @param srcPath
     * @param quality
     * @param outPath
     * @return 代表是否执行了压缩
     */
    public static boolean compressOringinal(String srcPath, int quality, String outPath) {
        File file = new File(srcPath);
        if (!shouldCompress(file, true)) {
            LogUtils.d(srcPath + ",no need to compress");
            return false;
        }

        //todo 过大的图,resize到1600w像素,仿照谷歌.
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(srcPath);
        } catch (Throwable throwable) {
            LogUtils.w(srcPath + " , decode bitmap failed:" + throwable.getMessage());
        }

        if (bitmap == null) {
            return false;
        }
        boolean success = false;
        File outFile = new File(outPath);
        try {
            success = compressOringinal2(srcPath, quality, outPath);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            //success = compressByAndroid(bitmap,quality,outPath);
        }

        if (success && outFile.exists() && outFile.length() > 50) {
            //如果压缩后的图比压缩前还大,那么就不压缩,返回原图
            if (file.length() < outFile.length()) {
                LogUtils.i(outFile.getAbsolutePath() + "  file.length() < outFile.length(), ignore");
                deleteFile(outFile);
                success = false;
            } else {
                success = true;

            }

        } else {
            LogUtils.w(outFile.getAbsolutePath() + "  compress progress fail:", success, outFile.exists(), outFile.length());
            success = false;
        }
        return success;
    }


    private static boolean compressOringinal2(String inputPath, int quality, String outPath) {
        try {
            File file = new File(outPath);
            Bitmap bitmap = BitmapFactory.decodeFile(inputPath);

            ExifInterface exifInterface = new ExifInterface(inputPath);
            int oritation = exifInterface.getRotationDegrees();
            Map<String, String> exifMap = ExifUtil.readExif(inputPath);
            ;
            if (oritation != 0) {
                try {
                    bitmap = rotaingImageView(oritation, bitmap);
                    exifMap.put(ExifInterface.TAG_ORIENTATION, "0");
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LogUtils.w("compress fail when rotate");
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            boolean success = bitmap.compress(compressToWebp ? Bitmap.CompressFormat.WEBP: Bitmap.CompressFormat.JPEG,
                    compressToWebp ? targetWebpQuality : quality,
                    fileOutputStream);
            success = success && file.exists() && file.length() > 50;
            CloseUtils.closeIO(fileOutputStream);

            //motion image

            if (success) {
                try {
                    if (exifMap != null && !exifMap.isEmpty()) {
                    }else {
                        exifMap = new HashMap<>();
                    }
                    boolean editSoftware = true;
                    if(editSoftware){
                        String text = exifMap.get(ExifInterface.TAG_SOFTWARE);
                        String tail = "jpg-q-"+targetJpgQuality;
                        if(compressToWebp){
                            tail = "webp-q-"+targetWebpQuality;
                        }
                        if(TextUtils.isEmpty(text)){
                            text = AppUtils.getAppPackageName().substring(AppUtils.getAppPackageName().lastIndexOf(".")+1)+"/"+AppUtils.getAppVersionName()+"/compressor/"+tail;
                        }else {
                            text = text + "/"+AppUtils.getAppPackageName().substring(AppUtils.getAppPackageName().lastIndexOf(".")+1)+"/"+AppUtils.getAppVersionName()+"/compressor/"+tail;
                        }
                        exifMap.put(ExifInterface.TAG_SOFTWARE,text);
                    }


                    ExifUtil.writeExif(exifMap, outPath);

                    //motion photo处理: 视频压缩,更改xml写exif-> 合并文件
                    boolean motionImage = MotionPhotoUtil.isMotionImage(inputPath, true);
                    if(motionImage){
                        String mp4 = MotionPhotoUtil.getMotionVideoPath(inputPath);
                        File originalMp4File = new File(mp4);
                        File mp4Compressed = null;
                        if(originalMp4File.length() < 1024*1024){
                            //1M以下不压缩
                            mp4Compressed = originalMp4File;
                        }else {
                             mp4Compressed = AndroidMotionImpl.compressMp4File(mp4);
                        }

                        if(mp4Compressed !=null && mp4Compressed.exists() && mp4Compressed.length() >0){
                            long length = mp4Compressed.length();
                            //保存为谷歌格式/小米格式/原格式
                            if(length != originalMp4File.length()){
                                //更改exif
                                ExifInterface exifInterface1 = new ExifInterface(outPath);
                                String xmp = exifInterface1.getAttribute(ExifInterface.TAG_XMP);
                                LogUtils.i("xmp before",xmp);
                                xmp = xmp.replace(originalMp4File.length()+"",length+"");
                                LogUtils.i("xmp after",xmp);
                                exifInterface1.setAttribute(ExifInterface.TAG_XMP,xmp);
                                //写exif
                                exifInterface1.saveAttributes();
                            }

                            //合并文件:
                            FileIOUtils.writeFileFromIS(new File(outPath),new FileInputStream(mp4Compressed),true);
                            mp4Compressed.delete();
                            new File(mp4).delete();
                        }
                    }

                    /*if(AppUtils.isAppDebug()){
                        ExifUtil.readJpgTail(absolutePath);
                    }*/
                    //ExifUtil.copyMotionPhotoJpegTail(absolutePath,outPath);
                    return true;
                } catch (Throwable e) {
                    LogUtils.w(outPath + ",write exif failed:" + e.getMessage());
                    e.printStackTrace();
                }
            }
            return false;

        } catch (Throwable e) {
            LogUtils.w(outPath, "compress fail by e", e.getClass().getSimpleName(), e.getMessage());
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
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return new Magick().getJPEGImageQuality(inputStream);
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
