package com.hss01248.image;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;

import com.hss01248.image.config.GlobalConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
 * Created by Administrator on 2017/3/15 0015.
 */

public class MyUtil {


    public static int dip2px(float dipValue){
        final float scale = GlobalConfig.context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }


    /**
     * 等比压缩（宽高等比缩放）
     * @param bitmap
     * @param needRecycle
     * @param targetWidth
     * @param targeHeight
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bitmap, boolean needRecycle, int targetWidth, int targeHeight) {
        float sourceWidth = bitmap.getWidth();
        float sourceHeight = bitmap.getHeight();

        float scaleWidth = targetWidth / sourceWidth;
        float scaleHeight = targeHeight / sourceHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight); //长和宽放大缩小的比例
        Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (needRecycle) {
            bitmap.recycle();
        }
        bitmap = bm;
        return bitmap;
    }



    public static String getRealType(File file){
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] b = new byte[4];
            try {
                is.read(b, 0, b.length);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
            String type =  bytesToHexString(b).toUpperCase();
            if(type.contains("FFD8FF")){
                return "jpg";
            }else if(type.contains("89504E47")){
                return "png";
            }else if(type.contains("47494638")){
                return "gif";
            }else if(type.contains("49492A00")){
                return "tif";
            }else if(type.contains("424D")){
                return "bmp";
            }
            return type;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static String appendUrl(String url) {
        String newUrl = url;
        if(TextUtils.isEmpty(newUrl)){
            return newUrl;
        }
        boolean hasHost = url.contains("http:" ) || url.contains("https:" )  ;
        if (!hasHost){
           if(!TextUtils.isEmpty(GlobalConfig.baseUrl)){
               newUrl = GlobalConfig.baseUrl+url;
           }
        }

        return newUrl;
    }


    public static OkHttpClient getClient(boolean ignoreCertificateVerify){
        if(ignoreCertificateVerify){
            return getAllPassClient();
        }else {
            return getNormalClient();
        }
    }

    /**
     * 不校验证书
     * @return
     */
    private static OkHttpClient getAllPassClient() {

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

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // sslSocketFactory和hostnameVerifier代码与httpsUtil中一模一样,只有这里不一样,但下面能行,这里就不行,见鬼了
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(DO_NOT_VERIFY)
                .readTimeout(0, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS).writeTimeout(0, TimeUnit.SECONDS) //设置超时
                .build();

        return client;
    }

    private static OkHttpClient getNormalClient(){
        OkHttpClient client = new OkHttpClient.Builder()
                //.sslSocketFactory(sslContext.getSocketFactory())
                //.hostnameVerifier(DO_NOT_VERIFY)
                .readTimeout(0, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS).writeTimeout(0, TimeUnit.SECONDS) //设置超时
                .build();
        return client;
    }
}
