package com.hss01248.image.interfaces;

import android.content.Context;
import android.view.View;

import com.hss01248.image.config.SingleConfig;

import java.io.File;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

public interface ILoader {

    void init(Context context,int cacheSizeInM);

    void request(SingleConfig config);

    void pause();

    void resume();

    void clearDiskCache();

    void clearMomoryCache();

    long getCacheSize();

    void clearCacheByUrl(String url);

    void clearMomoryCache(View view);
    void clearMomoryCache(String url);

    File getFileFromDiskCache(String url);

    void getFileFromDiskCache(String url,FileGetter getter);





   boolean  isCached(String url);

    void trimMemory(int level);

    void onLowMemory();


    /**
     * 如果有缓存,就直接从缓存里拿,如果没有,就从网上下载
     * 返回的file在图片框架的缓存中,非常规文件名,需要自己拷贝出来.
     * @param url
     * @param getter
     */
    void download(String url,FileGetter getter);

}
