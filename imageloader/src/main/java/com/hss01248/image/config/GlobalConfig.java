package com.hss01248.image.config;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import com.blankj.utilcode.util.AppUtils;
import com.hss01248.image.R;
import com.hss01248.image.interfaces.ILoader;
import com.hss01248.image.interfaces.ImageLoaderExceptionHandler;
import com.hss01248.image.interfaces.LoadInterceptor;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class GlobalConfig {

    public static String baseUrl;

    public static Context context;
    public static boolean useThirdPartyGifLoader = false;

    public static Handler getMainHandler() {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    private static Handler mainHandler;
    public static boolean debug = AppUtils.isAppDebug();

    public static int getWinHeight() {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return winHeight < winWidth ? winHeight : winWidth;
        } else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return winHeight > winWidth ? winHeight : winWidth;
        }
        return winHeight;
    }

    public static int getWinWidth() {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return winHeight > winWidth ? winHeight : winWidth;
        } else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return winHeight < winWidth ? winHeight : winWidth;
        }
        return winWidth;
    }

    private static int winHeight;
    private static int winWidth;
    private static boolean userFresco;

    public static ImageLoaderExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public static void setExceptionHandler(ImageLoaderExceptionHandler exceptionHandler) {
        GlobalConfig.exceptionHandler = exceptionHandler;
    }

    private static ImageLoaderExceptionHandler exceptionHandler;
    //private static int oritation;

    public static void init(Context context, int cacheSizeInM, ILoader imageLoader) {

        GlobalConfig.context = context;
        GlobalConfig.cacheMaxSize = cacheSizeInM;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        GlobalConfig.winWidth = wm.getDefaultDisplay().getWidth();
        GlobalConfig.winHeight = wm.getDefaultDisplay().getHeight();
        GlobalConfig.loader = imageLoader;
        //oritation = context.getResources().getConfiguration().orientation;
        //GlobalConfig.userFresco = userFresco;
        imageLoader.init(context, cacheSizeInM);


    }

    /**
     * lrucache 最大值
     */
    public static int cacheMaxSize = 1024;

    /**
     * 缓存文件夹
     */
    public static String cacheFolderName = "";

    /**
     * bitmap是888还是565编码,后者内存占用相当于前者一般,前者显示效果要好一点点,但两者效果不会差太多
     */
    public static boolean highQuality = false;

    /**
     * https是否忽略校验,默认不忽略
     */

    public static boolean ignoreCertificateVerify = false;


    public static void setBigImageDark(boolean isBigImageDark) {
        GlobalConfig.isBigImageDark = isBigImageDark;
    }

    public static boolean isBigImageDark = true;

    public static void setLoader(ILoader loader) {
        GlobalConfig.loader = loader;
    }

    private static ILoader loader;

    public static ILoader getLoader() {
        return loader;
    }

    public static LoadInterceptor interceptor;

    public static int placeHolderResId = R.drawable.im_item_list_opt;
    public static int placeHolderScaleType = ScaleMode.FIT_CENTER;
    public static int errorResId = R.drawable.im_item_list_opt_error;
    public static int errorScaleType = ScaleMode.FIT_CENTER;
    public static int loadingResId = R.drawable.iv_loading_trans;
    public static int loadingScaleType = ScaleMode.FIT_CENTER;

    public static int scaleType = ScaleMode.FIT_CENTER;

    public static void setDefaultPlaceHolder(int placeHolderResId) {
        GlobalConfig.placeHolderResId = placeHolderResId;
    }

    public static void setDefaultPlaceHolderScaleType(int placeHolderScaleType) {
        GlobalConfig.placeHolderScaleType = placeHolderScaleType;
    }

    public static void setDefaultErrorHolder(int errorResId) {
        GlobalConfig.errorResId = errorResId;
    }

    public static void setDefaultErrorHolderScaleType(int errorScaleType) {
        GlobalConfig.errorScaleType = errorScaleType;
    }

    public static void setDefaultLoadingHolder(int loadingResId) {
        GlobalConfig.loadingResId = loadingResId;
    }


}
