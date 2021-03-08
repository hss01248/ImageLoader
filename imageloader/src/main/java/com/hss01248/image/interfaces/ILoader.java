package com.hss01248.image.interfaces;

import android.content.Context;
import android.view.View;

import com.hss01248.image.config.SingleConfig;

import java.io.File;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

public abstract class ILoader {

    public abstract void init(Context context, int cacheSizeInM);


    public abstract void requestAsBitmap(SingleConfig config);

    public abstract void requestForNormalDiaplay(SingleConfig config);

    public abstract void debug(SingleConfig config);


    public abstract void pause();

    public abstract void resume();

    public abstract void clearDiskCache();

    public abstract void clearMomoryCache();

    public abstract long getCacheSize();

    public abstract void clearCacheByUrl(String url);

    public abstract void clearMomoryCache(View view);

    public abstract void clearMomoryCache(String url);

    public abstract File getFileFromDiskCache(String url);

    public abstract void getFileFromDiskCache(String url, FileGetter getter);


    public abstract boolean isCached(String url);

    public abstract void trimMemory(int level);

    public abstract void onLowMemory();


    /**
     * 如果有缓存,就直接从缓存里拿,如果没有,就从网上下载
     * 返回的file在图片框架的缓存中,非常规文件名,需要自己拷贝出来.
     *
     * @param url
     * @param getter
     */
    public abstract void download(String url, FileGetter getter);

}
