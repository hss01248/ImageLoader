package com.hss01248.glideloader;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.GlideModule;
import com.hss01248.image.config.GlobalConfig;

/**
 * Created by Administrator on 2017/5/2 0002.
 */

public class GlideModelConfig implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
       // builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);

        builder.setDiskCache(new DiskLruCacheFactory( GlobalConfig.cacheFolderName,GlobalConfig.cacheMaxSize*1024*1024));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
