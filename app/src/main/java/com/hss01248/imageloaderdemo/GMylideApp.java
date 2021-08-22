package com.hss01248.imageloaderdemo;


import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@GlideModule
public class GMylideApp extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //设置缓存到外部存储器
        //builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context));
        long memoryCacheSizeBytes = 1024L * 1024 * 1024 * 2; // 2G
        //        设置内存缓存大小
        //builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
        //        根据SD卡是否可用选择是在内部缓存还是SD卡缓存
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR, memoryCacheSizeBytes));
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        //registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(ProgressManager.getInstance()
                .with(new OkHttpClient.Builder()).build()));
                       // .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build()));
    }
}
