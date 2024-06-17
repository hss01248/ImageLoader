package com.hss01248.media.localvideoplayer.media;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ScrollView;

import androidx.annotation.ColorRes;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.media.localvideoplayer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by huangshuisheng on 2017/10/26.
 */

public class ScreenSnapShotUtil {







    public static Bitmap screenShoot(Dialog dialog) {
        View decorView = dialog.getWindow().getDecorView();
        decorView.setDrawingCacheEnabled(true);
        decorView.buildDrawingCache();
        Bitmap bmp = decorView.getDrawingCache();
        decorView.destroyDrawingCache();
        return bmp;
    }

    /**
     * 截屏并保存到dcim下,并且通知mediacenter,让系统图库能够马上看到这张图
     */
    public static void screenShootAndSave(Dialog dialog,boolean toastResult, MyCommonCallback<String> callback) {
        Window view = dialog.getWindow();
        screenShootAndSave(view != null ? view.getDecorView() : null,toastResult,callback);
    }

    public static void screenShootAndSave(Activity activity,boolean toastResult, MyCommonCallback<String> callback) {
        screenShootAndSave(activity.getWindow().getDecorView(),toastResult,callback);
    }

    public static void screenShootAndSave(View view,boolean toastResult, MyCommonCallback<String> callback) {
        screenShootAndSave(view, 0,toastResult,callback);
    }

    public static void screenShootAndSave(View view, @ColorRes int colorResId,
                                          boolean toastResult, MyCommonCallback<String> callback) {
        if (view == null) {
            return;
        }

        try {
            if (view instanceof ScrollView) {
                Bitmap bmp = getBitmapByView((ScrollView) view, colorResId);
                requestStoragePermissionAndSave(bmp, view,toastResult,callback);
            } else {
                View decorView = view;
                decorView.setDrawingCacheEnabled(true);
                decorView.buildDrawingCache();
                Bitmap bmp = decorView.getDrawingCache();
                requestStoragePermissionAndSave(bmp, decorView,toastResult,callback);
            }
        } catch (Throwable e) {
           LogUtils.w(e);
           if(toastResult)
            ToastUtils.showLong(getString2(R.string.deliver_error_save_failed,view.getContext()));
            if(callback != null)
            callback.onError("Java Exception",e.getMessage(),e);
        }

    }

    private static String getString2(int deliver_error_save_failed, Context context) {
        return context.getResources().getString(deliver_error_save_failed);
    }

    public static void saveBitmap(Bitmap bmp,  boolean showToast, MyCommonCallback<String> callback) {



        File dir = getAlbumDir(Utils.getApp());
       // bmp = mixTransInPng(bmp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToGallery2(Utils.getApp(),bmp,showToast,callback);
            //view.destroyDrawingCache();
            return;
        }
        if (!dir.exists()) {
            boolean mk = dir.mkdirs();
            if (!mk) {
                if(showToast)
                    ToastUtils.showLong(getString2(R.string.deliver_error_save_failed,Utils.getApp()));
                if(callback != null){
                    callback.onError("-1","cannot create dir",null);
                }
            }
        }
        File file = new File(dir, AppUtils.getAppName()+"_"+System.currentTimeMillis() + "-screenshot.png");
        boolean saved = saveBitmapToFile(file.getAbsolutePath(), bmp,80, Bitmap.CompressFormat.PNG);
        if (!saved) {
            if(showToast)
                ToastUtils.showLong(getString2(R.string.deliver_error_save_failed,Utils.getApp()));
            if(callback != null){
                callback.onError("-1","save image failed",null);
            }
            return;
        }
        //view.destroyDrawingCache();
        //立刻通知系统
        MediaStoreRefresher.refreshMediaCenter(Utils.getApp(), file.getAbsolutePath());
        bmp.recycle();
        if(showToast)
            ToastUtils.showLong(getString2(R.string.saved_success,Utils.getApp()));
        if(callback != null){
            callback.onSuccess(file.getAbsolutePath());
        }
    }

    private static void requestStoragePermissionAndSave(Bitmap bmp, View view,  boolean showToast, MyCommonCallback<String> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveBitmap(bmp,showToast, callback);
            return;
        }
        if(PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            saveBitmap(bmp, showToast, callback);
        }else {
            PermissionUtils.permission(PermissionConstants.STORAGE).callback(new PermissionUtils.SimpleCallback() {
                @Override
                public void onGranted() {
                    saveBitmap(bmp, showToast, callback);
                }

                @Override
                public void onDenied() {
                    saveBitmap(bmp,showToast, callback);
                }
            }).request();
        }

    }

    /**
     * 保存图片到文件
     */
    private static boolean saveBitmapToFile(String filename, Bitmap bitmap, int quality,
                                            Bitmap.CompressFormat compressFormat) {
        if (null == bitmap || TextUtils.isEmpty(filename)) {
            return false;
        }
        FileOutputStream fos = null;
        try {
            File file = new File(filename);
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fos = new FileOutputStream(file);
            bitmap.compress(compressFormat, quality, fos);
            fos.flush();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static File getAlbumDir(Context context) {
        File dir =  null;
        if(PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Screenshots");
        }else {
            dir =  context.getExternalFilesDir("snapshot-"+context.getPackageName());
        }
        if(!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }

    private static Bitmap getBitmapByView(ScrollView scrollView, @ColorRes int colorId) {
        int h = 0;
        Bitmap bitmap = null;
        // 获取scrollview实际高度
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            View child = scrollView.getChildAt(i);
            if (child == null || child.getVisibility() == View.GONE) {
                continue;
            }
            h += child.getHeight();
            if (colorId != 0) {
                scrollView.getChildAt(i).setBackgroundColor(scrollView.getContext().getResources().getColor(colorId));
            }else {
                scrollView.getChildAt(i).setBackgroundColor(Color.WHITE);
            }
        }
        // 创建对应大小的bitmap
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        return bitmap;
    }


    /**
     * android 11及以上保存图片到相册
     * @param context
     * @param image
     */
    private static void saveImageToGallery2(Context context, Bitmap image,boolean showToast, MyCommonCallback<String> callback){
        Long mImageTime = System.currentTimeMillis();
        String imageDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(mImageTime));
        String SCREENSHOT_FILE_NAME_TEMPLATE = AppUtils.getAppName()+ "_%s.jpg";//图片名称
        String mImageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate);

        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES
                + File.separator + AppUtils.getAppName()); //Environment.DIRECTORY_SCREENSHOTS:截图,图库中显示的文件夹名。"dh"
        //DIRECTORY_SCREENSHOTS--> 不允许操作
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, mImageFileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATE_ADDED, mImageTime / 1000);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, mImageTime / 1000);
        values.put(MediaStore.MediaColumns.DATE_EXPIRES, (mImageTime + DateUtils.DAY_IN_MILLIS) / 1000);
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);

        ContentResolver resolver = context.getContentResolver();
        //requires android.permission.WRITE_EXTERNAL_STORAGE
         Uri uri = null;
        try {
             uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            // First, write the actual data for our screenshot
            try (OutputStream out = resolver.openOutputStream(uri)) {
                if (!image.compress(Bitmap.CompressFormat.JPEG, 80, out)) {
                    throw new IOException("Failed to compress");
                }
            }
            // Everything went well above, publish it!
            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            values.putNull(MediaStore.MediaColumns.DATE_EXPIRES);
            resolver.update(uri, values, null, null);
            if(showToast){
                ToastUtils.showLong(getString2(R.string.saved_success,context));
            }
            if(callback != null){
                callback.onSuccess(uri.toString());
            }
        }catch (Throwable e){
            if(showToast){
                ToastUtils.showLong(getString2(R.string.deliver_error_save_failed,context)+":\n"+e.getMessage());
            }
            if(callback != null){
                callback.onError("Java Exception",e.getMessage(),e);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && uri != null) {
                resolver.delete(uri, null);
            }
            LogUtils.w(e);
        }
    }


    public static Bitmap mixTransInPng(Bitmap tagBitmap){
        //原bitmap是imutable,不能直接更改像素点,要新建bitmap,像素编辑后设置
        boolean isPngWithTransAlpha = hasTransInAlpha(tagBitmap);
        if(!isPngWithTransAlpha){
            return tagBitmap;
        }
        int w = tagBitmap.getWidth();
        int h = tagBitmap.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int tintBgColorIfHasTransInAlpha = 0x00ffffff;
        long start = System.currentTimeMillis();
        out:
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                // The argb {@link Color} at the specified coordinate
                int pix = tagBitmap.getPixel(i, j);
                long alpha = ((pix >> 24) & 0xff);/// 255.0f
                // Log.d("luban","位置:"+i+"-"+j+", 不透明度:"+a);
                //255就是没有透明度, = 0 就是完全透明. 值代表不透明度.值越大,越不透明
                if (alpha != 255) {
                    //半透明时,白色化,而不是像Android原生内部实现一样简单粗暴地将不透明度设置为0,一片黑色

                    //策略1: 不混合颜色,只区分0和255.只要有半透明,就使用前景色  性能还可以
                           /* if(alpha == 0){
                                tintBgColorIfHasTransInAlpha = tintBgColorIfHasTransInAlpha | 0xff000000;
                                pix = tintBgColorIfHasTransInAlpha ;
                                //也可以改成外部传入背景色
                            }else {
                               pix = pix | 0xff000000;
                            }*/

                    //策略2: 颜色混合:  显示颜色= 前景色* alpha/255 + 背景色 * (255 - alpha)/255.
                    if (alpha == 0) {
                        //将alpha改成255.完全不透明
                        pix = tintBgColorIfHasTransInAlpha | 0xff000000;
                    } else {
                               /* 要使用rgb三个通道分别计算,而不能作为一个int值整体计算:
                               long pix2 = (long) (pix * alpha/255f +  tintBgColorIfHasTransInAlpha * (255f-alpha) / 255f);
                                pix2 = pix2 | 0xff000000; */

                        int r = ((pix >> 16) & 0xff);
                        int g = ((pix >> 8) & 0xff);
                        int b = ((pix) & 0xff);

                        int br = ((tintBgColorIfHasTransInAlpha >> 16) & 0xff);
                        int bg = ((tintBgColorIfHasTransInAlpha >> 8) & 0xff);
                        int bb = ((tintBgColorIfHasTransInAlpha) & 0xff);

                        int fr = Math.round((r * alpha + br * (255 - alpha)) / 255f);
                        int fg = Math.round((g * alpha + bg * (255 - alpha)) / 255f);
                        int fb = Math.round((b * alpha + bb * (255 - alpha)) / 255f);

                        // 注意是用或,不是用加: pix = 0xff << 24 + fr << 16 + fg << 8 + fb;
                        pix = (0xff << 24) | (fr << 16) | (fg << 8) | fb;
                        //等效: Color.argb(0xff,fr,fg,fb);
                    }
                    bitmap.setPixel(i, j, pix);
                } else {
                    bitmap.setPixel(i, j, pix);
                }
            }
        }
        LogUtils.d("半透明通道颜色混合 cost(ms):" , (System.currentTimeMillis() - start));
        return bitmap;
    }


    public static boolean hasTransInAlpha(Bitmap bitmap) {
        if (!bitmap.getConfig().equals(Bitmap.Config.ARGB_8888)) {
            return false;
        }
        int w = bitmap.getWidth() - 1;
        int h = bitmap.getHeight() - 1;
        if (isTrans(bitmap, 0, 0)
                || isTrans(bitmap, w, h)
                || isTrans(bitmap, 0, h)
                || isTrans(bitmap, w, 0)
                || isTrans(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2)) {
            //先判断4个顶点和中心.
            return true;
        }
        //然后折半查找
        return hasTransInAngel(bitmap, w, h);
    }

    private static boolean hasTransInAngel(Bitmap bitmap, int w, int h) {
        Log.d("ss", "hastrans: porint:" + w + "-" + h);
        // int[][] arr = new int[8][2];
        if (w == 0 || h == 0) {
            return false;
        }
        int halfw = w / 2;
        int halfh = h / 2;

        boolean hasTrans = isTrans(bitmap, w, h)
                || isTrans(bitmap, w, 0)
                || isTrans(bitmap, 0, h)
                || isTrans(bitmap, w, halfh)
                || isTrans(bitmap, halfw, h)
                || isTrans(bitmap, 0, halfh)
                || isTrans(bitmap, halfw, 0);
        if (hasTrans) {
            return hasTrans;
        }
        return hasTransInAngel(bitmap, halfw, halfh);
    }

    private static boolean isTrans(Bitmap bitmap, int x, int y) {
        int pix = bitmap.getPixel(x, y);
        int a = ((pix >> 24) & 0xff);
        return a != 255;
    }


}
