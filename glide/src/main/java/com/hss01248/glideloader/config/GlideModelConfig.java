package com.hss01248.glideloader.config;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpGlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.model.GlideUrl;
import com.github.piasy.biv.progress.ProgressInterceptor;

import okhttp3.OkHttpClient;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/5/2 0002.
 */

public class GlideModelConfig extends OkHttpGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        }else {
            builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
        }//解决rgb565部分手机上出现绿色问题
        //比较耗时,所以反向设置
       /* builder.setDiskCache(new DiskLruCacheFactory(new File(context.getCacheDir(), GlobalConfig.cacheFolderName).getAbsolutePath(),
                GlobalConfig.cacheMaxSize*1024*1024));*/
        Log.i("glide","applyOptions---");

       /* builder.setResizeService(new FifoPriorityThreadPoolExecutor(4))
                .setDiskCacheService(new FifoPriorityThreadPoolExecutor(4));*/

    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        /**
         * 不带拦截功能，只是单纯替换通讯组件
         */

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        setIgnoreAll(builder);
        OkHttpClient client=builder
                .addNetworkInterceptor(new ProgressInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
        Log.i("glide","registerComponents---");

    }


    private static void setIgnoreAll(OkHttpClient.Builder builder){
        X509TrustManager xtm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[]{};
                return x509Certificates;
                // return null;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

            HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            builder.sslSocketFactory(sslContext.getSocketFactory())
                    .hostnameVerifier(DO_NOT_VERIFY);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
