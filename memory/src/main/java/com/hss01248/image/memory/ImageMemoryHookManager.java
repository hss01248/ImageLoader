package com.hss01248.image.memory;//


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.ColorSpace;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.DisplayMetrics;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

import static android.content.Context.SENSOR_SERVICE;


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
        if(!bitmaps.isEmpty()){
            Collections.sort(bitmaps, new Comparator<Bitmap>() {
                @Override
                public int compare(Bitmap o1, Bitmap o2) {
                    return (int) (getSize(o2) - getSize(o1));
                }
            });
        }
        return bitmaps;
    }

     static long getSize(Bitmap bitmap){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        }else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }



    public static void hook(final Application application) {

        SensorManager sensorManager = (SensorManager) application.getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(new ShakeDetector.Listener() {
            @Override
            public void hearShake() {
                show(application);
            }
        });
        sd.start(sensorManager);



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DexposedBridge.findAndHookMethod(BitmapFactory.class, "decodeStream",
                            InputStream.class, Rect.class, Options.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                           add(param);
                        }

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }


                    });
                    DexposedBridge.findAndHookMethod(BitmapFactory.class, "decodeByteArray",
                            byte[].class, Integer.TYPE, Integer.TYPE, Options.class,
                            new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            add(param);
                        }
                    });
                    DexposedBridge.findAndHookMethod(BitmapFactory.class, "decodeFileDescriptor",

                            FileDescriptor.class, Rect.class, Options.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            add(param);
                        }
                    });

                    //调用到nativeCreate相关的java方法
                    DexposedBridge.findAndHookMethod(Bitmap.class, "createBitmap",
                            Bitmap.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Matrix.class, Boolean.TYPE,
                            new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            add(param);
                        }
                    });
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        DexposedBridge.findAndHookMethod(Bitmap.class, "createBitmap",
                                DisplayMetrics.class,Integer.TYPE, Integer.TYPE, Config.class,Boolean.TYPE, ColorSpace.class,
                                new XC_MethodHook() {
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    }

                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                        add(param);
                                    }
                                });
                    }
                    //public static Bitmap createBitmap(@NonNull DisplayMetrics display,
                    //            @NonNull @ColorInt int[] colors, int offset, int stride,
                    //            int width, int height, @NonNull Config config)
                    DexposedBridge.findAndHookMethod(Bitmap.class, "createBitmap",
                            DisplayMetrics.class,int[].class,Integer.TYPE, Integer.TYPE,Integer.TYPE, Integer.TYPE, Config.class,
                            new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                }

                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    add(param);
                                }
                            });
                    //Bitmap.createBitmap(width, height, Config.ARGB_8888)
                    DexposedBridge.findAndHookMethod(Bitmap.class, "createBitmap",
                            Integer.TYPE, Integer.TYPE, Config.class,
                            new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            add(param);
                        }
                    });

                    DexposedBridge.findAndHookMethod(Bitmap.class, "createBitmap",
                            int[].class, Integer.TYPE, Integer.TYPE, Config.class,
                            new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            add(param);
                        }
                    });
                    DexposedBridge.findAndHookMethod(Bitmap.class, "copy",
                            Config.class, Boolean.TYPE,
                                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                           add(param);
                        }
                    });


//

                    /*Class<?> cls = Class.forName("android.graphics.Bitmap$BitmapFinalizer");
                    DexposedBridge.findAndHookMethod(cls, "finalize", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Integer hashcode = param.thisObject.hashCode();
                           removeBitmap(hashcode);

                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        }
                    });*/
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
                    .append(",内存占用:").append(formatFileSize(getSize(bitmap))).append(",config:")
            .append(bitmap.getConfig().name());

        return builder.toString();
    }

    public static void show(Context activity){
        Intent intent = new Intent(activity,ImgMemoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }



}
