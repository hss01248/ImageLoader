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

    void clearCacheByUrl(String url);

    void clearMomoryCache(View view);
    void clearMomoryCache(String url);

    File getFileFromDiskCache(String url);

   boolean  isCached(String url);

    void trimMemory(int level);

    void clearAllMemoryCaches();

}
