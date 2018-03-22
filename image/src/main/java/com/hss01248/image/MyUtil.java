package com.hss01248.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.utils.RoundedCornersTransformation2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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







    public static SingleConfig.BitmapListener getBitmapListenerProxy(final SingleConfig.BitmapListener listener){
        return (SingleConfig.BitmapListener) Proxy.newProxyInstance(SingleConfig.class.getClassLoader(),
                listener.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {

                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Object object=  method.invoke(listener,args);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return null;
            }
        });

    }


    public static void runOnUIThread(Runnable runnable){
        GlobalConfig.getMainHandler().post(runnable);
    }


    public static void viewBigImage(SingleConfig config) {
        BigImageView bigImageView = (BigImageView) config.getTarget();
        bigImageView.showImage(buildUriByType(config));
        //bigimageview对缩略图的支持并不好
       /* if(TextUtils.isEmpty(config.getThumbnailUrl())){
            if(!TextUtils.isEmpty(config.getUrl()) && !isCached(config.getUrl() )){
                bigImageView.setProgressIndicator(new ProgressPieIndicator1());
            }
            bigImageView.showImage(buildUriByType(config));
        }else {
            bigImageView.showImage(Uri.parse(config.getThumbnailUrl()),buildUriByType(config));
        }*/
    }




    public static boolean shouldSetPlaceHolder(SingleConfig config){
        if(config.isReuseable()){
            return true;
        }
        if(config.getPlaceHolderResId()<=0 ) {
            return false;
        }


        if(config.getResId()>0 || !TextUtils.isEmpty(config.getFilePath()) || GlobalConfig.getLoader().isCached(config.getUrl())){
            return false;
        }else {//只有在图片源为网络图片,并且图片没有缓存到本地时,才给显示placeholder
            return true;
        }
    }


    public static int dip2px(float dipValue){
        if(dipValue<=0){
            return (int) dipValue;
        }
        final float scale = GlobalConfig.context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

    public static int px2dp(float pxValue){
        final float scale = GlobalConfig.context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale + 0.5f);
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


    /**
     * 类型	SCHEME	示例
     远程图片	http://, https://	HttpURLConnection 或者参考 使用其他网络加载方案
     本地文件	file://	FileInputStream
     Content provider	content://	ContentResolver
     asset目录下的资源	asset://	AssetManager
     res目录下的资源	res://	Resources.openRawResource
     Uri中指定图片数据	data:mime/type;base64,	数据类型必须符合 rfc2397规定 (仅支持 UTF-8)
     * @param config
     * @return
     */
    public static Uri buildUriByType(SingleConfig config) {

        Log.e("builduri:","url: "+config.getUrl()+" ---filepath:"+config.getFilePath()+ "--content:"+config.getContentProvider());

        if(!TextUtils.isEmpty(config.getUrl())){
            String url = MyUtil.appendUrl(config.getUrl());
            return Uri.parse(url);
        }

        if(config.getResId() > 0){
            return Uri.parse("res://imageloader/" + config.getResId());
        }

        if(!TextUtils.isEmpty(config.getFilePath())){

            File file = new File(config.getFilePath());
            if(file.exists()){
                return Uri.fromFile(file);
            }
        }

        if(!TextUtils.isEmpty(config.getContentProvider())){
            String content = config.getContentProvider();
            if(!content.startsWith("content")){
                content = "content://"+content;
            }
            return Uri.parse(content);
        }




        return null;
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


    /**
     * 获取指定文件夹内所有文件大小的和
     *
     * @param file file
     * @return size
     * @throws Exception
     */
    public static long getFolderSize(File file)  {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 删除指定目录下的文件，这里用于缓存的删除
     *
     * @param filePath filePath
     * @param deleteThisPath deleteThisPath
     */
    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (File file1 : files) {
                        deleteFolderFile(file1.getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {
                        file.delete();
                    } else {
                        if (file.listFiles().length == 0) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static long getCacheSize() {
        File dir = new File(ImageLoader.context.getCacheDir(), GlobalConfig.cacheFolderName);
        if(dir!=null && dir.exists()){
            return MyUtil.getFolderSize(dir);
        }else {
            return 0;
        }

    }



    public static Bitmap rectRound(Bitmap source,int radius, int margin){
        return new RoundedCornersTransformation2(radius,margin).transform(source,source.getWidth(),source.getHeight());
    }

    public static Bitmap cropCirle(Bitmap source,boolean recycleOriginal) {
        //BitmapPool mBitmapPool = Glide.get(BigLoader.context).getBitmapPool();


        int size = Math.min(source.getWidth(), source.getHeight());

        int width = (source.getWidth() - size) / 2;
        int height = (source.getHeight() - size) / 2;
        //source.setHasAlpha(true);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        //bitmap.setHasAlpha(true);



        Canvas canvas = new Canvas(bitmap);

        //canvas.drawColor(Color.TRANSPARENT);
        //canvas.setBitmap(bitmap);
        Paint paint = new Paint();
        //paint.setColor(Color.TRANSPARENT);
        //paint.setColorFilter()
        BitmapShader shader =
                new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        if (width != 0 || height != 0) {
            // source isn't square, move viewport to center
            Matrix matrix = new Matrix();
            matrix.setTranslate(-width, -height);
            shader.setLocalMatrix(matrix);
        }
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        if(recycleOriginal){
            source.recycle();
        }

        return bitmap;
    }

    public static ImageView.ScaleType getScaleTypeForImageView(int scaleMode,boolean isContent){
        switch (scaleMode){
            case 0:
                if(isContent){
                    return ImageView.ScaleType.CENTER_CROP;
                }else {
                    return ImageView.ScaleType.CENTER_INSIDE;
                }

            case ScaleMode.CENTER_CROP:
                return ImageView.ScaleType.CENTER_CROP;
            case ScaleMode.FIT_XY:
                return ImageView.ScaleType.FIT_XY;
            case ScaleMode.CENTER:
                return ImageView.ScaleType.CENTER;
            case ScaleMode.FIT_CENTER:
                return ImageView.ScaleType.FIT_CENTER;
            case ScaleMode.FIT_START:
                return ImageView.ScaleType.FIT_START;
            case ScaleMode.CENTER_INSIDE:
                return ImageView.ScaleType.CENTER_INSIDE;
            default:
                if(isContent){
                    return ImageView.ScaleType.CENTER_CROP;
                }else {
                    return ImageView.ScaleType.CENTER_INSIDE;
                }
        }
    }

    public static int[] getImageWidthHeight(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        return new int[]{options.outWidth,options.outHeight};
    }















}
