package com.hss01248.glidev4.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.LibraryGlideModule;
import com.hss01248.glide.aop.net.ModifyResponseBodyInterceptor;
import com.hss01248.image.config.GlobalConfig;

import java.io.InputStream;
import java.nio.ByteBuffer;
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

import jp.co.link_u.library.glideavif.AvifDecoderFromByteBuffer;
import me.jessyan.progressmanager.ProgressManager;
import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2017/5/2 0002.
 */
@GlideModule
public class GlideModelConfig extends LibraryGlideModule {


    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide,
                                   @NonNull Registry registry) {
        /**
         * 不带拦截功能，只是单纯替换通讯组件
         */
        LogUtils.i("in lib: registerComponents");
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
                //.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        //if(GlobalConfig.debug){
            setIgnoreAll(builder);
       // }

        builder
                .addNetworkInterceptor(new ModifyResponseBodyInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(ProgressManager.getInstance()
                .with(builder).build()));
        registry.prepend(ByteBuffer.class, Bitmap.class,new AvifDecoderFromByteBuffer());
        //todo 不如直接用aop切,简单粗暴: com.bumptech.glide.load.model.FileLoader.buildLoadData, 将第一个参数File model替换掉
/*

        registry.replace(File.class, InputStream.class, new MyStreamFactory());

        registry.replace(File.class, ParcelFileDescriptor.class, new MyFileDescriptorFactory());*/
        Log.i("glide", "registerComponents---");


        //file:
        // .append(File.class, ByteBuffer.class, new ByteBufferFileLoader.Factory())
        //        .append(File.class, InputStream.class, new FileLoader.StreamFactory())
        //        .append(File.class, File.class, new FileDecoder())
        //        .append(File.class, ParcelFileDescriptor.class, new FileLoader.FileDescriptorFactory())
        //        // Compilation with Gradle requires the type to be specified for UnitModelLoader here.
        //        .append(File.class, File.class, UnitModelLoader.Factory.<File>getInstance())

        // .append(String.class, ParcelFileDescriptor.class, new StringLoader.FileDescriptorFactory())
        //  .append(
        //            Uri.class,
        //            ParcelFileDescriptor.class,
        //            new AssetUriLoader.FileDescriptorFactory(context.getAssets()))

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //      registry.append(
        //          Uri.class, InputStream.class, new QMediaStoreUriLoader.InputStreamFactory(context));
        //      registry.append(
        //          Uri.class,
        //          ParcelFileDescriptor.class,
        //          new QMediaStoreUriLoader.FileDescriptorFactory(context));
        //    }
        //    registry
        //        .append(Uri.class, InputStream.class, new UriLoader.StreamFactory(contentResolver))
        //        .append(
        //            Uri.class,
        //            ParcelFileDescriptor.class,
        //            new UriLoader.FileDescriptorFactory(contentResolver))
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
