package com.hss01248.imageloaderdemo;


import android.content.Context;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class GMylideApp extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        LogUtils.w("in app: applyOptions");
        //设置缓存到外部存储器
        //builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context));
        long memoryCacheSizeBytes = 1024L * 1024 * 1024 * 2; // 2G
        //        设置内存缓存大小
        //builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
        //        根据SD卡是否可用选择是在内部缓存还是SD卡缓存

        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR, memoryCacheSizeBytes));
    }

    //@Override
   // public void registerComponents(Context context, Glide glide, Registry registry) {
       // LogUtils.w("in app: registerComponents");
        //registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
       /* registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(ProgressManager.getInstance()
                .with(new OkHttpClient.Builder()).build()));
        registry.prepend(ByteBuffer.class, Bitmap.class,new AvifDecoderFromByteBuffer());*/
                       // .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build()));
    //}
}
