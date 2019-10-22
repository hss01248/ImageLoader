package com.hss01248.image.memory;//


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 图片内存hook 接口，将app在使用过程中 取得内存中的所有图片
 * Created by penghanying on 2019/10/14.
 */
public class ImageMemoryHookManager {

    private static Map<Integer,WeakReference<Bitmap>> sBitmapReference = new HashMap<>();

    private static ImageMemoryHookManager instance;



    private static   void addBitmap(Bitmap bitmap) {
        sBitmapReference.put(bitmap.hashCode(),new WeakReference<Bitmap>(bitmap));
    }

    private static void removeBitmap(Integer integer) {
        sBitmapReference.remove(integer);
    }

    static List<Bitmap> getList(){
        List<Bitmap> bitmaps = new ArrayList<>();
        for (Map.Entry<Integer, WeakReference<Bitmap>> entry : sBitmapReference.entrySet()) {
            WeakReference<Bitmap> weakReference = entry.getValue();
            if(weakReference != null && weakReference.get() != null && !weakReference.get().isRecycled()){
                bitmaps.add(weakReference.get());
            }
        }
        return bitmaps;
    }



    public static void hook(final Application application) {
        if( Build.VERSION.SDK_INT > 23){
            return;
        }

        ShakeUtils shakeUtils = new ShakeUtils(application);
        shakeUtils.setOnShakeListener(new ShakeUtils.OnShakeListener() {
            @Override
            public void onShake() {
                show(application);
            }
        });



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DexposedBridge.findAndHookMethod(BitmapFactory.class, "decodeStream", new Object[]{InputStream.class, Rect.class, Options.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                           add(param);
                        }

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }


                    }});
                    DexposedBridge.findAndHookMethod(BitmapFactory.class, "decodeByteArray", new Object[]{byte[].class, Integer.TYPE, Integer.TYPE, Options.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            add(param);
                        }
                    }});
                    DexposedBridge.findAndHookMethod(BitmapFactory.class, "decodeFileDescriptor", new Object[]{FileDescriptor.class, Rect.class, Options.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            add(param);
                        }
                    }});
                    DexposedBridge.findAndHookMethod(Bitmap.class, "createBitmap", new Object[]{Bitmap.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Matrix.class, Boolean.TYPE, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            add(param);
                        }
                    }});
                    DexposedBridge.findAndHookMethod(Bitmap.class, "createBitmap", new Object[]{Integer.TYPE, Integer.TYPE, Config.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            add(param);
                        }
                    }});
                    DexposedBridge.findAndHookMethod(Bitmap.class, "createBitmap", new Object[]{int[].class, Integer.TYPE, Integer.TYPE, Config.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            add(param);
                        }
                    }});
                    DexposedBridge.findAndHookMethod(Bitmap.class, "copy", new Object[]{Config.class, Boolean.TYPE, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                           add(param);
                        }
                    }});
//

                    Class<?> cls = Class.forName("android.graphics.Bitmap$BitmapFinalizer");
                    DexposedBridge.findAndHookMethod(cls, "finalize", new Object[]{new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Integer hashcode = param.thisObject.hashCode();
                           removeBitmap(hashcode);

                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        }
                    }});
                } catch (Throwable var2) {
                    var2.printStackTrace();
                }
            }
        }).start();
    }

    private static void add(XC_MethodHook.MethodHookParam param) {
        try {
            addBitmap((Bitmap) param.getResult());
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

    }



     static String formatFileSize(long size) {
        try {
            DecimalFormat dff = new DecimalFormat(".00");
            if (size >= 1024 * 1024) {
                double doubleValue = ((double) size) / (1024 * 1024);
                String value = dff.format(doubleValue);
                return value + "MB";
            } else if (size > 1024) {
                double doubleValue = ((double) size) / 1024;
                String value = dff.format(doubleValue);
                return value + "KB";
            } else {
                return size + "B";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(size);
    }

     static String getInfo(Bitmap bitmap){
        StringBuilder builder = new StringBuilder();
            builder.append(bitmap.getWidth()).append("x").append(bitmap.getHeight())
                    .append(",size:").append(formatFileSize(bitmap.getByteCount())).append(",config:")
            .append(bitmap.getConfig().name());

        return builder.toString();
    }

    public static void show(Context activity){
        Intent intent = new Intent(activity,ImgMemoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }



}
