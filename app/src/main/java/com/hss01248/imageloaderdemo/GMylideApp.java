package com.hss01248.imageloaderdemo;


import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.OkHttpClient;

@GlideModule
public class GMylideApp extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //设置缓存到外部存储器
        builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context));
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        //registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(ProgressManager.getInstance()
                .with(new OkHttpClient.Builder()).build()));
    }
}
