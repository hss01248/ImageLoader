package com.hss01248.image.config;

import android.content.Context;

import com.hss01248.image.fresco.FrescoLoader;
import com.hss01248.image.interfaces.ILoader;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class GlobalConfig {

    public static String baseUrl;

    public static Context context;
    /**
     * lrucache 最大值
     */
    public static int cacheMaxSize= 50;

    /**
     * 缓存文件夹
     */
    public static String cacheFolderName = "frescoCache";

    /**
     * bitmap是888还是565编码,后者内存占用相当于前者一般,前者显示效果要好一点点,但两者效果不会差太多
     */
    public static boolean highQuality = false;

    /**
     * https是否忽略校验,默认不忽略
     */

    public static boolean ignoreCertificateVerify = false;

    private static ILoader loader;

    public static ILoader getLoader() {
        if(loader == null){
            loader = new FrescoLoader();
        }
        return loader;
    }


}
