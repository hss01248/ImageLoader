package com.hss01248.glidev4.config;

import android.content.Context;

import androidx.annotation.NonNull;

import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.LibraryGlideModule;
import com.github.piasy.biv.progress.ProgressInterceptor;

import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2017/5/2 0002.
 */
@GlideModule
public class GlideModelConfig extends LibraryGlideModule {

    /* @Override
     public void applyOptions(Context context, GlideBuilder builder) {
         builder.setLogLevel(Log.DEBUG);
         if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
             builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
         }else {
             builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
         }//解决rgb565部分手机上出现绿色问题

         *//*final UncaughtThrowableStrategy myUncaughtThrowableStrategy = new ...
    builder.setDiskCacheExecutor(newDiskCacheExecutor(myUncaughtThrowableStrategy));
    builder.setResizeExecutor(newSourceExecutor(myUncaughtThrowableStrategy));*//*
    }
*/
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide,
                                   @NonNull Registry registry) {
        /**
         * 不带拦截功能，只是单纯替换通讯组件
         */

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        setIgnoreAll(builder);
        OkHttpClient client = builder
                .addNetworkInterceptor(new ProgressInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
        Log.i("glide", "registerComponents---");

    }


    private static void setIgnoreAll(OkHttpClient.Builder builder) {
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
