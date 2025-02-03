package com.hss01248.image;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.load.HttpException;
import com.bumptech.glide.load.engine.GlideException;
import com.hss01248.image.config.GlobalConfig;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.image.config.SingleConfig;
import com.hss01248.image.exif.ExifUtil;
import com.hss01248.image.utils.RoundedCornersTransformation2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.util.List;
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


    @SuppressWarnings("unchecked")
    public static <T> T getProxy(final T listener) {
        return (T) Proxy.newProxyInstance(listener.getClass().getClassLoader(),
                listener.getClass().getInterfaces(), new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                        try {
                            Object object = method.invoke(listener, args);
                            return object;
                        } catch (Throwable e) {
                            handleException(e);
                            return null;
                        }
                    }
                });

    }

    public static void handleException(Throwable e) {
        if (e == null) {
            return;
        }
        if (GlobalConfig.getExceptionHandler() != null) {
            try {
                GlobalConfig.getExceptionHandler().onError(e);
            } catch (Throwable e2) {
                e2.printStackTrace();
            }
        } else {
            e.printStackTrace();
        }
    }


    public static void runOnUIThread(Runnable runnable) {
        GlobalConfig.getMainHandler().post(runnable);
    }


    public static void viewBigImage(SingleConfig config) {

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


    public static boolean shouldSetPlaceHolder(SingleConfig config) {
        if (config.isReuseable()) {
            return true;
        }
        if (config.getPlaceHolderResId() <= 0) {
            return false;
        }
        if (!TextUtils.isEmpty(config.getSourceString())) {
            if (config.getSourceString().startsWith("http")) {
                //只有在图片源为网络图片,并且图片没有缓存到本地时,才给显示placeholder
                return true;
            }
        }
        return false;
    }


    public static int dip2px(float dipValue) {
        if (dipValue <= 0) {
            return (int) dipValue;
        }
        final float scale = GlobalConfig.context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dp(float pxValue) {
        final float scale = GlobalConfig.context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * 等比压缩（宽高等比缩放）
     *
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


    public static String getRealType(File file) {
        if (!file.exists()) {
            return "";
        }
        if (file.getName().endsWith(".gif")) {
            return "gif";
        }
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
            String type = bytesToHexString(b).toUpperCase();
            if (type.contains("FFD8FF")) {
                return "jpg";
            } else if (type.contains("89504E47")) {
                return "png";
            } else if (type.contains("47494638")) {
                return "gif";
            } else if (type.contains("49492A00")) {
                return "tif";
            } else if (type.contains("424D")) {
                return "bmp";
            }
            return type;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
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
     * 远程图片	http://, https://	HttpURLConnection 或者参考 使用其他网络加载方案
     * 本地文件	file://	FileInputStream
     * Content provider	content://	ContentResolver
     * asset目录下的资源	asset://	AssetManager
     * res目录下的资源	res://	Resources.openRawResource
     * Uri中指定图片数据	data:mime/type;base64,	数据类型必须符合 rfc2397规定 (仅支持 UTF-8)
     *
     * @param config
     * @return
     */
    public static Uri buildUriByType(SingleConfig config) {

        // Log.i("builduri:", "url: " + config.getUrl() + " ---filepath:" + config.getFilePath() + "--content:" + config
        //       .getContentProvider());

        if (!TextUtils.isEmpty(config.getSourceString())) {
            //String url = MyUtil.appendUrl(config.getUrl());
            if (config.getSourceString().startsWith("/storage/")) {
                File file = new File(config.getSourceString());
                return Uri.fromFile(file);
            } else if (config.getSourceString().startsWith("data:mime/type;base64")) {
                return Uri.parse(config.getSourceString());
            }
            return Uri.parse(config.getSourceString());
        }

        if (config.getResId() > 0) {
            return Uri.parse("res://imageloader/" + config.getResId());
        }

        if (config.getBytes() != null) {
            //?
        }

        return null;
    }

    public static String getUsablePath(SingleConfig config) {
        // Log.i("builduri:", "url: " + config.getUrl() + " ---filepath:" + config.getFilePath() + "--content:" + config
        //    .getContentProvider());

        if (!TextUtils.isEmpty(config.getSourceString())) {
            //String url = MyUtil.appendUrl(config.getUrl());
            return config.getSourceString();
        }

        if (config.getResId() > 0) {
            return "res://imageloader/" + config.getResId();
        }

        return "";
    }


    public static String appendUrl(String url) {
        String newUrl = url;
        if (TextUtils.isEmpty(newUrl)) {
            return newUrl;
        }
        boolean hasHost = url.contains("http:") || url.contains("https:");
        if (!hasHost) {
            if (!TextUtils.isEmpty(GlobalConfig.baseUrl)) {
                newUrl = GlobalConfig.baseUrl + url;
            }
        }

        return newUrl;
    }


    public static OkHttpClient getClient(boolean ignoreCertificateVerify) {
        if (ignoreCertificateVerify) {
            return getAllPassClient();
        } else {
            return getNormalClient();
        }
    }

    /**
     * 不校验证书
     *
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

    private static OkHttpClient getNormalClient() {
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
     */
    public static long getFolderSize(File file) {
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
     * @param filePath       filePath
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
        if (dir != null && dir.exists()) {
            return MyUtil.getFolderSize(dir);
        } else {
            return 0;
        }

    }


    public static Bitmap rectRound(Bitmap source, int radius, int margin) {
        return new RoundedCornersTransformation2(radius, margin)
                .transform(source, source.getWidth(), source.getHeight());
    }

    public static Bitmap cropCirle(Bitmap source, boolean recycleOriginal) {
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
        if (recycleOriginal) {
            source.recycle();
        }

        return bitmap;
    }

    public static ImageView.ScaleType getScaleTypeForImageView(int scaleMode, boolean isContent) {
        return getScaleTypeForImageView(scaleMode, isContent, false);
    }

    public static ImageView.ScaleType getScaleTypeForImageView(int scaleMode, boolean isContent, boolean isGlide) {
        switch (scaleMode) {
            case 0:
                if (isContent) {
                    return ImageView.ScaleType.FIT_CENTER;
                } else {
                    return ImageView.ScaleType.CENTER_INSIDE;
                }

            case ScaleMode.CENTER_CROP:
                if (isContent && isGlide) {
                    return ImageView.ScaleType.FIT_CENTER;
                }
                return ImageView.ScaleType.CENTER_CROP;
            case ScaleMode.FIT_XY:
                return ImageView.ScaleType.FIT_XY;
            case ScaleMode.CENTER:
                return ImageView.ScaleType.CENTER;
            case ScaleMode.FIT_CENTER:
                return ImageView.ScaleType.FIT_CENTER;
            case ScaleMode.FIT_START:
                return ImageView.ScaleType.FIT_START;
            case ScaleMode.FIT_END:
                return ImageView.ScaleType.FIT_END;
            case ScaleMode.CENTER_INSIDE:
                return ImageView.ScaleType.CENTER_INSIDE;
            default:
                if (isContent) {
                    return ImageView.ScaleType.FIT_CENTER;
                } else {
                    return ImageView.ScaleType.CENTER_INSIDE;
                }
        }
    }

    public static int[] getImageWidthHeight(String path) {
        if(path.endsWith(".avif")){
             return new int[]{-1, -1};
        }
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         *options.outHeight为原始图片的高
         */
        return new int[]{options.outWidth, options.outHeight};
    }


    public static Activity getActivityFromContext(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static String formatFileSize(long size) {
        try {
            DecimalFormat dff = new DecimalFormat(".00");
            if (size >= 1024 * 1024) {
                double doubleValue = ((double) size) / (1024 * 1024);
                String value = dff.format(doubleValue);
                return value + "MB";
            } else if (size > 1024) {
                double doubleValue = ((double) size) / 1024;
                String value = dff.format(doubleValue);
                return value + "KB";
            } else {
                return size + "B";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(size);
    }

    /**
     * 复制文本到剪贴板
     *
     * @param text 文本
     */
    public static void copyText(final CharSequence text) {
        //部分机型出现SecurityException
        //http://10.0.20.7/zentao/bug-view-22357.html
        try {
            ClipboardManager clipboard = (ClipboardManager) Utils.getApp().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("text", text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String printBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return "null";
        }
        StringBuilder stringBuilder = new StringBuilder("bitmap:\n");
        stringBuilder.append(bitmap.getWidth())
                .append("x")
                .append(bitmap.getHeight())
                .append(",")
                .append(bitmap.getConfig().name())
                .append(",isRecycled:")
                .append(bitmap.isRecycled())
                .append(",\nByteCount:")
                .append(formatFileSize(bitmap.getByteCount()))
                .append("\ndensity:")
                .append(bitmap.getDensity())
                .append("\nhasAlpha:")
                .append(bitmap.hasAlpha());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            stringBuilder.append("\n,hasMipMap:").append(bitmap.hasMipMap());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            stringBuilder.append("\nallocationByteCount:").append(formatFileSize(bitmap.getAllocationByteCount()));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stringBuilder.append("\nColorSpace:").append(bitmap.getColorSpace());
        }
        stringBuilder.append("\nGenerationId:").append(bitmap.getGenerationId());
        return stringBuilder.toString();
    }

    public static String printImageView(ImageView imageView) {
        if (imageView == null) {
            return "null";
        }

        StringBuilder stringBuilder = new StringBuilder("imageView:\n");
        stringBuilder.append(imageView.getMeasuredWidth())
                .append("x")
                .append(imageView.getMeasuredHeight())
                .append("\nscaleType:")
                .append(imageView.getScaleType().name())
                .append("\nLayoutParams:")
                .append(logWH(imageView.getLayoutParams().width))
                .append("x")
                .append(logWH(imageView.getLayoutParams().height))
                .append("\nid:")
                .append(getIdName(imageView))
                .append("\ntag:")
                .append(imageView.getTag());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            stringBuilder.append("\n,CropToPadding:").append(imageView.getCropToPadding());
            stringBuilder.append("\nAdjustViewBounds:").append(imageView.getAdjustViewBounds());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (imageView.getClipBounds() != null) {
                stringBuilder.append("\nClipBounds:").append(imageView.getClipBounds().toString());
            }
            stringBuilder.append("\nImageAlpha:").append(imageView.getImageAlpha());
            stringBuilder.append("\nAlpha:").append(imageView.getAlpha());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stringBuilder.append("\nTintMode:").append(imageView.getImageTintMode());
        }
        stringBuilder.append("\nforground:");
        stringBuilder.append(imageView.getDrawable());
        if (imageView.getDrawable() != null) {
            stringBuilder.append(",").append(imageView.getDrawable().getIntrinsicWidth())
                    .append("x").append(imageView.getDrawable().getIntrinsicHeight());
        }
        stringBuilder.append("\nbackground:");
        stringBuilder.append(imageView.getBackground()).append("\n");
        stringBuilder.append(imageView.toString());
        return stringBuilder.toString();
    }

    private static String getIdName(View view) {
        if (view == null) {
            return "";
        }
        try {
            return view.getResources().getResourceEntryName(view.getId());
        } catch (Throwable e) {
            e.printStackTrace();
            return view.getId() + "";
        }

    }

    public static String logWH(int wh) {
        if (wh > 0) {
            return wh + "";
        }
        if (wh == ViewGroup.LayoutParams.MATCH_PARENT) {
            return " MATCH_PARENT ";
        } else if (wh == ViewGroup.LayoutParams.WRAP_CONTENT) {
            return " WRAP_CONTENT ";
        } else {
            return wh + "";
        }
    }

    public static String printException(Throwable e) {
        if (e == null) {
            //LogUtils.eTag("glide-ex1"," e is null");
            return "exception is null";
        }
         e = unwrapGlideException(e);

        while (e.getCause() !=null){
            e = e.getCause();
        }
        if(e instanceof HttpException){
            HttpException httpException = (HttpException) e;
            //LogUtils.iTag("glide-ex4",httpException.getStatusCode(),httpException);
            return "exception:\nHttpException: http code " + httpException.getStatusCode() +","+httpException.getMessage();
        }
        //LogUtils.iTag("glide-ex5",e);
        return "exception:\n" + e.getClass().getSimpleName() + ":" + e.getMessage();
    }

    public static Throwable realException(Throwable e) {
        if (e == null) {
            //LogUtils.eTag("glide-ex1"," e is null");
            return new IOException("null");
        }
        e = unwrapGlideException(e);

        while (e.getCause() !=null){
            e = e.getCause();
        }
        if(e instanceof HttpException){
            HttpException httpException = (HttpException) e;
            //LogUtils.iTag("glide-ex4",httpException.getStatusCode(),httpException);
            return new HttpException("http code " + httpException.getStatusCode() +","+httpException.getMessage());
        }
        return e;
    }

    private static Throwable unwrapGlideException(Throwable e) {
        if(e instanceof GlideException){
            GlideException exception = (GlideException) e;
            List<Throwable> rootCauses = exception.getRootCauses();
            //exception.logRootCauses("glide-ex33");
            Throwable exception1 = exception;
            if(rootCauses !=null && rootCauses.size()>0){
                exception1 = rootCauses.get(0);
                return  unwrapGlideException(exception1);
            }else{
                return exception1;
            }
        }else {
            return e;
        }
    }

    public static boolean isBitmapTooLarge(float bw, float bh, ImageView imageView) {

        float bitmapArea = bw * bh;
        float ivArea = imageView.getMeasuredWidth() * imageView.getMeasuredHeight();
        if (ivArea == 0) {
            return false;
        }
        return (bitmapArea / ivArea) > 1.25f;
    }

    public static int calculateScaleRatio(int width, int height, int reqWidth, int reqHeight, int multiplRatio) {

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            //计算图片高度和我们需要高度的最接近比例值
            final int heightRatio = Math.round(((float) height / (float) reqHeight) * multiplRatio);
            //宽度比例值
            final int widthRatio = Math.round(((float) width / (float) reqWidth) * multiplRatio);
            //取比例值中的较大值作为inSampleSize
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * https://www.exif.org/Exif2-2.PDF
     *
     * @param filePath
     * @return
     */
    public static String printExif(String filePath) {
        return ExifUtil.readExif(filePath);
    }






/*---------------------
    作者：yyanjun
    来源：CSDN
    原文：https://blog.csdn.net/yyanjun/article/details/79896677
    版权声明：本文为博主原创文章，转载请附上博文链接！*/


}
